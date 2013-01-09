/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package mao.util;

import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;

/**
 * Utility functions for handling files.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class FileUtils {
    private static final String TAG="FileUtils";

  private FileUtils() {
    // Utility class.
  }

  public static int chmod(File path, int mode) throws Exception {
    Class<?> fileUtils = Class.forName("android.os.FileUtils");
    Method setPermissions =
        fileUtils.getMethod("setPermissions", String.class, int.class, int.class, int.class);
    return (Integer) setPermissions.invoke(null, path.getAbsolutePath(), mode, -1, -1);
  }

  public static int getPermissions(File path) throws Exception {
    Class<?> fileUtils = Class.forName("android.os.FileUtils");
    int[] result = new int[1];
    Method getPermissions = fileUtils.getMethod("getPermissions", String.class, int[].class);
    getPermissions.invoke(null, path.getAbsolutePath(), result);
    return result[0];
  }

  private static boolean recursiveChmod(File root, int mode) throws Exception {
    boolean success = chmod(root, mode) == 0;
    for (File path : root.listFiles()) {
      if (path.isDirectory()) {
        success = recursiveChmod(path, mode);
      }
      success &= (chmod(path, mode) == 0);
    }
    return success;
  }

  public static boolean delete(File path) {
    boolean result = true;
    if (path.exists()) {
      if (path.isDirectory()) {
        for (File child : path.listFiles()) {
          result &= delete(child);
        }
        result &= path.delete(); // Delete empty directory.
      }
      if (path.isFile()) {
        result &= path.delete();
      }
      if (!result) {
        Log.e(TAG,"Delete failed;");
      }
      return result;
    } else {
      Log.e(TAG,"File does not exist.");
      return false;
    }
  }

  private static boolean makeDirectories(File directory, int mode) {
    File parent = directory;
    while (parent.getParentFile() != null && !parent.exists()) {
      parent = parent.getParentFile();
    }
    if (!directory.exists()) {
      Log.v(TAG,"Creating directory: " + directory.getName());
      if (!directory.mkdirs()) {
        Log.e(TAG,"Failed to create directory.");
        return false;
      }
    }
    try {
      recursiveChmod(parent, mode);
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  public static boolean rename(File file, String name) {
    return file.renameTo(new File(file.getParent(), name));
  }

  public static class FileStatus {
  }
}
