package com.vladsch.flexmark.ext.abbreviation.internal;

import com.vladsch.flexmark.ext.abbreviation.AbbreviationBlock;
import com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension;
import com.vladsch.flexmark.internal.util.DataHolder;
import com.vladsch.flexmark.internal.util.DataKey;
import com.vladsch.flexmark.internal.util.KeepType;
import com.vladsch.flexmark.internal.util.NodeRepository;

@SuppressWarnings("WeakerAccess")
public class AbbreviationRepository extends NodeRepository<AbbreviationBlock> {

    public AbbreviationRepository(DataHolder options) {
        super(options);
    }

    @Override
    public DataKey<AbbreviationRepository> getDataKey() {
        return AbbreviationExtension.ABBREVIATIONS;
    }

    @Override
    public DataKey<KeepType> getKeepDataKey() {
        return AbbreviationExtension.ABBREVIATIONS_KEEP;
    }
}