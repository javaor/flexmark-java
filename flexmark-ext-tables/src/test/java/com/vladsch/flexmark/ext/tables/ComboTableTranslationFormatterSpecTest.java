package com.vladsch.flexmark.ext.tables;

import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.formatter.RenderPurpose;
import com.vladsch.flexmark.formatter.TranslationHandler;
import com.vladsch.flexmark.html.renderer.HeaderIdGenerator;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.spec.SpecExample;
import com.vladsch.flexmark.test.ComboSpecTestCase;
import com.vladsch.flexmark.test.FlexmarkSpecExampleRenderer;
import com.vladsch.flexmark.test.SpecExampleRenderer;
import com.vladsch.flexmark.util.ast.IRender;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.DataKey;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.format.options.DiscretionaryText;
import com.vladsch.flexmark.util.format.options.TableCaptionHandling;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.*;

public class ComboTableTranslationFormatterSpecTest extends ComboSpecTestCase {
    private static final String SPEC_RESOURCE = "/ext_tables_translation_formatter_spec.md";
    private static final boolean SHOW_INTERMEDIATE = false;
    private static final DataHolder OPTIONS = new MutableDataSet()
            //.set(FormattingRenderer.INDENT_SIZE, 2)
            //.set(HtmlRenderer.PERCENT_ENCODE_URLS, true)
            //.set(Parser.EXTENSIONS, Collections.singleton(FormatterExtension.create()))
            .set(Parser.EXTENSIONS, Collections.singleton(TablesExtension.create()))
            .set(Parser.BLANK_LINES_IN_AST, true)
            .set(Parser.HTML_FOR_TRANSLATOR, true)
            .set(Parser.PARSE_INNER_HTML_COMMENTS, true)
            .set(Parser.HEADING_NO_ATX_SPACE, true)
            .set(Formatter.MAX_TRAILING_BLANK_LINES, 0);

    static final DataKey<Boolean> DETAILS = new DataKey<>("DETAILS", SHOW_INTERMEDIATE);

    private static final Map<String, DataHolder> optionsMap = new HashMap<>();
    static {
        //optionsMap.put("src-pos", new MutableDataSet().set(HtmlRenderer.SOURCE_POSITION_ATTRIBUTE, "md-pos"));
        //optionsMap.put("option1", new MutableDataSet().set(FormatterExtension.FORMATTER_OPTION1, true));
        optionsMap.put("gfm", new MutableDataSet()
                .set(TablesExtension.COLUMN_SPANS, false)
                .set(TablesExtension.APPEND_MISSING_COLUMNS, true)
                .set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
                .set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true)
        );
        optionsMap.put("details", new MutableDataSet().set(DETAILS, true));
        optionsMap.put("no-caption", new MutableDataSet().set(TablesExtension.FORMAT_TABLE_CAPTION, TableCaptionHandling.REMOVE));
        optionsMap.put("no-alignment", new MutableDataSet().set(TablesExtension.FORMAT_TABLE_APPLY_COLUMN_ALIGNMENT, false));
        optionsMap.put("no-width", new MutableDataSet().set(TablesExtension.FORMAT_TABLE_ADJUST_COLUMN_WIDTH, false));
        optionsMap.put("keep-whitespace", new MutableDataSet().set(TablesExtension.FORMAT_TABLE_TRIM_CELL_WHITESPACE, false));
        optionsMap.put("lead-trail-pipes", new MutableDataSet().set(TablesExtension.FORMAT_TABLE_LEAD_TRAIL_PIPES, false));
        optionsMap.put("space-around-pipe", new MutableDataSet().set(TablesExtension.FORMAT_TABLE_SPACE_AROUND_PIPES, false));
        optionsMap.put("adjust-column-width", new MutableDataSet().set(TablesExtension.FORMAT_TABLE_ADJUST_COLUMN_WIDTH, false));
        optionsMap.put("apply-column-alignment", new MutableDataSet().set(TablesExtension.FORMAT_TABLE_APPLY_COLUMN_ALIGNMENT, false));
        optionsMap.put("fill-missing-columns", new MutableDataSet().set(TablesExtension.FORMAT_TABLE_FILL_MISSING_COLUMNS, true));
        optionsMap.put("left-align-marker-as-is", new MutableDataSet().set(TablesExtension.FORMAT_TABLE_LEFT_ALIGN_MARKER, DiscretionaryText.AS_IS));
        optionsMap.put("left-align-marker-add", new MutableDataSet().set(TablesExtension.FORMAT_TABLE_LEFT_ALIGN_MARKER, DiscretionaryText.ADD));
        optionsMap.put("left-align-marker-remove", new MutableDataSet().set(TablesExtension.FORMAT_TABLE_LEFT_ALIGN_MARKER, DiscretionaryText.REMOVE));
    }

    static final Parser PARSER = Parser.builder(OPTIONS).build();
    // The spec says URL-escaping is optional, but the examples assume that it's enabled.
    static final Formatter FORMATTER = Formatter.builder(OPTIONS).build();

    static CharSequence translate(CharSequence text) {
        StringBuilder sb = new StringBuilder();
        int iMax = text.length();
        for (int i = 0; i < iMax; i++) {
            char c = text.charAt(i);

            if ("htpcom".indexOf(c) != -1) {
                sb.append(c);
                continue;
            }

            if ("aeiouy".indexOf(c) != -1) {
                sb.append(c);
            }

            if (Character.isUpperCase(c)) {
                sb.append(Character.toLowerCase(c));
            } else if (Character.isLowerCase(c)) {
                sb.append(Character.toUpperCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb;
    }

    static class TranslationFormatter implements IRender {
        final Formatter myFormatter;

        public TranslationFormatter(Formatter formatter) {
            myFormatter = formatter;
        }

        @org.jetbrains.annotations.Nullable
        @Override
        public DataHolder getOptions() {
            return myFormatter.getOptions();
        }

        @Override
        public void render(@NotNull Node node, @NotNull Appendable output) {
            TranslationHandler handler = myFormatter.getTranslationHandler(new HeaderIdGenerator.Factory());
            String formattedOutput = myFormatter.translationRender(node, handler, RenderPurpose.TRANSLATION_SPANS);

            // now need to output translation strings, delimited
            List<String> translatingTexts = handler.getTranslatingTexts();

            boolean showIntermediate = node.getDocument().get(DETAILS);

            try {
                if (showIntermediate) {
                    output.append("--------------------------\n");
                    output.append(formattedOutput);
                    output.append("--------------------------\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            ArrayList<CharSequence> translatedTexts = new ArrayList<>(translatingTexts.size());
            for (CharSequence text : translatingTexts) {
                CharSequence translated = translate(text);
                translatedTexts.add(translated);
                try {
                    if (showIntermediate) {
                        output.append("<<<").append(text).append('\n');
                        output.append(">>>").append(translated).append('\n');
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // use the translations
            if (showIntermediate) {
                try {
                    output.append("--------------------------\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            handler.setTranslatedTexts(translatedTexts);
            String partial = myFormatter.translationRender(node, handler, RenderPurpose.TRANSLATED_SPANS);

            if (showIntermediate) {
                try {
                    output.append(partial);
                    output.append("--------------------------\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Node partialDoc = PARSER.parse(partial);
            String translated = myFormatter.translationRender(partialDoc, handler, RenderPurpose.TRANSLATED);
            try {
                output.append(translated);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @org.jetbrains.annotations.NotNull
        @Override
        public String render(@NotNull Node document) {
            StringBuilder sb = new StringBuilder();
            render(document, sb);
            return sb.toString();
        }

        @org.jetbrains.annotations.NotNull
        @Override
        public IRender withOptions(@org.jetbrains.annotations.Nullable DataHolder options) {
            return new TranslationFormatter(myFormatter.withOptions(options));
        }
    }

    final static TranslationFormatter TRANSLATION_FORMATTER = new TranslationFormatter(FORMATTER);

    private static DataHolder optionsSet(String optionSet) {
        if (optionSet == null) return null;
        return optionsMap.get(optionSet);
    }

    public ComboTableTranslationFormatterSpecTest(SpecExample example) {
        super(example);
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> data() {
        return getTestData(SPEC_RESOURCE);
    }

    @Nullable
    @Override
    public DataHolder options(String optionSet) {
        return optionsSet(optionSet);
    }

    @NotNull
    @Override
    public String getSpecResourceName() {
        return SPEC_RESOURCE;
    }

    @Override
    public @NotNull SpecExampleRenderer getSpecExampleRenderer(@Nullable DataHolder exampleOptions) {
        return new FlexmarkSpecExampleRenderer(exampleOptions, PARSER, TRANSLATION_FORMATTER, true);
    }
}
