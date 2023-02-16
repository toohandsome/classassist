package io.github.toohandsome.classassist.spi;

import io.github.toohandsome.classassist.util.ConfigUtil;

import java.io.IOException;
import java.util.*;

public class DefaultScanPath implements ScanPath {


    @Override
    public List<String> getScanPath() {
        return ConfigUtil.getConfig("scan");
    }


}
