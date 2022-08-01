package io.github.toohandsome.classassist.spi;

import java.io.IOException;
import java.util.List;

public interface ScanPath {
    List<String> getScanPath() throws IOException;
}
