package com.app4.project.timelapseserver.util;

import java.io.IOException;

public interface IOSupplier<T> {

  T get() throws IOException;

}
