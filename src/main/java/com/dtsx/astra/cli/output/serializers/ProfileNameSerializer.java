package com.dtsx.astra.cli.output.serializers;

import com.dtsx.astra.cli.config.ProfileName;

enum ProfileNameSerializer implements OutputSerializer<ProfileName> {
    INSTANCE;

    @Override
    public boolean canSerialize(Object o) {
        return o instanceof ProfileName;
    }

    @Override
    public String serializeAsHumanInternal(ProfileName s) {
        return s.toString();
    }

    @Override
    public Object serializeAsJsonInternal(ProfileName s) {
        return s.toString();
    }

    @Override
    public String serializeAsCsvInternal(ProfileName s) {
        return s.toString();
    }
}
