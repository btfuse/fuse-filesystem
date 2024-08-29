
/*
Copyright 2023 Breautek

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.breautek.fuse.filesystem;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import com.breautek.fuse.testtools.FuseTestAPIClient;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;

@RunWith(AndroidJUnit4.class)
public class FuseFilesystemPluginTest {

    static File dataDir = new File("/data/data/com.breautek.fuse.filesystem.test/files/");

    @Rule
    public ActivityScenarioRule<FuseFilesystemTestActivity> activityRule = new ActivityScenarioRule<>(FuseFilesystemTestActivity.class);

    @BeforeClass
    public static void setUp() {
        try {
            setupFilesDir();
            setupSizeTestFile();
            setupMkdirTest();
            setupReadTest();
            setupTruncateFile();
            setupAppendFile();
            setupWriteFile();
            setupRemoveTest();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setupFilesDir() throws IOException {
        File filesDir = new File("/data/data/com.breautek.fuse.filesystem.test/files");
        if (!filesDir.exists()) {
            filesDir.mkdir();
        }
    }

    private static void setupRemoveTest() throws IOException {
        File removeFileTest = new File("/data/data/com.breautek.fuse.filesystem.test/files/removeFileTest");
        if (!removeFileTest.createNewFile() && !removeFileTest.exists()) {
            throw new RuntimeException("Could not setup recursive remove test file");
        }

        File recursiveRemoveTest = new File("/data/data/com.breautek.fuse.filesystem.test/files/removeRecursiveTest/abc/def");
        if (!recursiveRemoveTest.mkdirs() && !recursiveRemoveTest.exists()) {
            throw new RuntimeException("Could not setup recursive remove test file");
        }
    }

    private static void setupWriteFile() throws IOException {
        File appendFile = new File("/data/data/com.breautek.fuse.filesystem.test/files/writeFileTest");
        File appendOffsetFile = new File("/data/data/com.breautek.fuse.filesystem.test/files/writeFileTestWithOffset");
        if (appendFile.exists()) {
            appendFile.delete();
        }

        if (appendOffsetFile.exists()) {
            appendOffsetFile.delete();
        }

        boolean _unused = appendFile.createNewFile();
        _unused = appendOffsetFile.createNewFile();
        FileOutputStream io = new FileOutputStream(appendFile, false);
        FileOutputStream appendIO = new FileOutputStream(appendOffsetFile, false);

        String content = "Initial State!";
        byte[] buffer = content.getBytes();

        io.write(buffer);
        appendIO.write(buffer);
        io.close();
        appendIO.close();
    }

    private static void setupAppendFile() throws IOException {
        File appendFile = new File("/data/data/com.breautek.fuse.filesystem.test/files/appendFileTest");

        boolean _unused = appendFile.createNewFile();
        FileOutputStream io = new FileOutputStream(appendFile, false);

        String content = "Initial State!";
        byte[] buffer = content.getBytes();

        io.write(buffer);
        io.close();
    }

    private static void setupTruncateFile() throws IOException {
        File truncateFile1 = new File("/data/data/com.breautek.fuse.filesystem.test/files/truncateTest1");
        File truncateFile2 = new File("/data/data/com.breautek.fuse.filesystem.test/files/truncateTest2");

        boolean _unused = truncateFile1.createNewFile();
        _unused = truncateFile2.createNewFile();
        FileOutputStream io = new FileOutputStream(truncateFile1);
        FileOutputStream io2 = new FileOutputStream(truncateFile2);

        String content = "Initial State!";
        byte[] buffer = content.getBytes();

        io.write(buffer);
        io2.write(buffer);

        io.close();
        io2.close();
    }

    private static void setupReadTest() throws IOException {
        File sizeTestFile = new File("/data/data/com.breautek.fuse.filesystem.test/files/readTest");

        boolean _unused = sizeTestFile.createNewFile();
        FileOutputStream io = new FileOutputStream(sizeTestFile);

        String content = "Hello Test File!";
        byte[] buffer = content.getBytes();

        io.write(buffer);

        io.close();
    }

    private static void setupMkdirTest() {
        File dir1 = new File(dataDir, "mkdirTest");
        File dir2 = new File(dataDir, "mkdirRecursionTest");
        if (dir1.exists()) {
            FileUtils.deleteRecursively(dir1);
        }
        if (dir2.exists()) {
            FileUtils.deleteRecursively(dir2);
        }
    }

    private byte[] createParamsBuffer(String data) {
        byte[] bytes = data.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length + 4);
        buffer.putInt(bytes.length).put(bytes);
        return buffer.array();
    }

    private byte[] createParamsBuffer(String data, byte[] content) {
        byte[] bytes = data.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length + 4 + content.length);
        buffer.putInt(bytes.length).put(bytes).put(content);
        return buffer.array();
    }

    private static void setupSizeTestFile() {
        File sizeTestFile = new File("/data/data/com.breautek.fuse.filesystem.test/files/sizeTestFile");
        try {
            boolean _unused = sizeTestFile.createNewFile();
            FileOutputStream io = new FileOutputStream(sizeTestFile);

            byte[] buffer = new byte[512];
            io.write(buffer);

            io.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public static void tearDown() {}

    @Test
    public void shouldBeDirectoryFileType() {
        activityRule.getScenario().onActivity(activity -> {
            int port = activity.getFuseContext().getAPIPort();
            String secret = activity.getFuseContext().getAPISecret();

            FuseTestAPIClient client;
            try {
                client = new FuseTestAPIClient.Builder()
                        .setFuseContext(activity.getFuseContext())
                        .setAPIPort(port)
                        .setAPISecret(secret)
                        .setPluginID("FuseFilesystem")
                        .setType("text/plain")
                        .setEndpoint("/file/type")
                        .setContent("file:///")
                        .build();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }

            FuseTestAPIClient.FuseAPITestResponse response = client.execute();
            assertEquals(200, response.getStatus());

            int intType = Integer.parseInt(response.readAsString());

            FuseFileType type = null;
            switch (intType) {
                case 0:
                    type = FuseFileType.FILE;
                    break;
                case 1:
                    type = FuseFileType.DIRECTORY;
                    break;
            }

            assertEquals(FuseFileType.DIRECTORY, type);
        });
    }

    @Test
    public void shouldHaveSizeOf512() {
        activityRule.getScenario().onActivity(activity -> {
            int port = activity.getFuseContext().getAPIPort();
            String secret = activity.getFuseContext().getAPISecret();

            FuseTestAPIClient client;
            try {
                client = new FuseTestAPIClient.Builder()
                        .setFuseContext(activity.getFuseContext())
                        .setAPIPort(port)
                        .setAPISecret(secret)
                        .setPluginID("FuseFilesystem")
                        .setType("text/plain")
                        .setEndpoint("/file/size")
                        .setContent("file:///data/data/com.breautek.fuse.filesystem.test/files/sizeTestFile")
                        .build();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }

            FuseTestAPIClient.FuseAPITestResponse response = client.execute();
            assertEquals(200, response.getStatus());

            long size = Long.parseLong(response.readAsString());

            assertEquals(512, size);
        });
    }

    @Test
    public void canMkdirWithoutRecursion() {
        activityRule.getScenario().onActivity(activity -> {
            int port = activity.getFuseContext().getAPIPort();
            String secret = activity.getFuseContext().getAPISecret();

            FuseTestAPIClient client;
            try {
                JSONObject content = new JSONObject();
                content.put("path", "file:///data/data/com.breautek.fuse.filesystem.test/files/mkdirTest");
                content.put("recursive", false);
                client = new FuseTestAPIClient.Builder()
                        .setFuseContext(activity.getFuseContext())
                        .setAPIPort(port)
                        .setAPISecret(secret)
                        .setPluginID("FuseFilesystem")
                        .setType("application/json")
                        .setEndpoint("/file/mkdir")
                        .setContent(content.toString())
                        .build();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }

            FuseTestAPIClient.FuseAPITestResponse response = client.execute();
            assertEquals(200, response.getStatus());

            String result = response.readAsString();

            assertEquals("true", result);
        });
    }

    @Test
    public void canMkdirWithRecursion() {
        activityRule.getScenario().onActivity(activity -> {
            int port = activity.getFuseContext().getAPIPort();
            String secret = activity.getFuseContext().getAPISecret();

            FuseTestAPIClient client;
            try {
                JSONObject content = new JSONObject();
                content.put("path", "file:///data/data/com.breautek.fuse.filesystem.test/files/mkdirRecursionTest/with/subfolders");
                content.put("recursive", true);
                client = new FuseTestAPIClient.Builder()
                        .setFuseContext(activity.getFuseContext())
                        .setAPIPort(port)
                        .setAPISecret(secret)
                        .setPluginID("FuseFilesystem")
                        .setType("application/json")
                        .setEndpoint("/file/mkdir")
                        .setContent(content.toString())
                        .build();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }

            FuseTestAPIClient.FuseAPITestResponse response = client.execute();
            assertEquals(200, response.getStatus());

            String result = response.readAsString();

            assertEquals("true", result);
        });
    }

    @Test
    public void canReadFileEntirely() {
        activityRule.getScenario().onActivity(activity -> {
            int port = activity.getFuseContext().getAPIPort();
            String secret = activity.getFuseContext().getAPISecret();

            FuseTestAPIClient client;
            try {
                JSONObject content = new JSONObject();
                content.put("path", "file:///data/data/com.breautek.fuse.filesystem.test/files/readTest");
                content.put("length", -1);
                content.put("offset", 0);
                client = new FuseTestAPIClient.Builder()
                        .setFuseContext(activity.getFuseContext())
                        .setAPIPort(port)
                        .setAPISecret(secret)
                        .setPluginID("FuseFilesystem")
                        .setType("application/json")
                        .setEndpoint("/file/read")
                        .setContent(content.toString())
                        .build();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }

            FuseTestAPIClient.FuseAPITestResponse response = client.execute();
            assertEquals(200, response.getStatus());

            String result = response.readAsString();

            assertEquals("Hello Test File!", result);
        });
    }

    @Test
    public void canReadFilePartially() {
        activityRule.getScenario().onActivity(activity -> {
            int port = activity.getFuseContext().getAPIPort();
            String secret = activity.getFuseContext().getAPISecret();

            FuseTestAPIClient client;
            try {
                JSONObject content = new JSONObject();
                content.put("path", "file:///data/data/com.breautek.fuse.filesystem.test/files/readTest");
                content.put("length", 2);
                content.put("offset", 0);
                client = new FuseTestAPIClient.Builder()
                        .setFuseContext(activity.getFuseContext())
                        .setAPIPort(port)
                        .setAPISecret(secret)
                        .setPluginID("FuseFilesystem")
                        .setType("application/json")
                        .setEndpoint("/file/read")
                        .setContent(content.toString())
                        .build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            FuseTestAPIClient.FuseAPITestResponse response = client.execute();
            assertEquals(200, response.getStatus());

            String result = response.readAsString();

            assertEquals("He", result);
        });
    }

    @Test
    public void canReadFileWithOffset() {
        activityRule.getScenario().onActivity(activity -> {
            int port = activity.getFuseContext().getAPIPort();
            String secret = activity.getFuseContext().getAPISecret();

            FuseTestAPIClient client;
            try {
                JSONObject content = new JSONObject();
                content.put("path", "file:///data/data/com.breautek.fuse.filesystem.test/files/readTest");
                content.put("length", 2);
                content.put("offset", 1);
                client = new FuseTestAPIClient.Builder()
                        .setFuseContext(activity.getFuseContext())
                        .setAPIPort(port)
                        .setAPISecret(secret)
                        .setPluginID("FuseFilesystem")
                        .setType("application/json")
                        .setEndpoint("/file/read")
                        .setContent(content.toString())
                        .build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            FuseTestAPIClient.FuseAPITestResponse response = client.execute();
            assertEquals(200, response.getStatus());

            String result = response.readAsString();

            assertEquals("el", result);
        });
    }

    @Test
    public void canTruncateFile() {
        activityRule.getScenario().onActivity(activity -> {
            int port = activity.getFuseContext().getAPIPort();
            String secret = activity.getFuseContext().getAPISecret();

            String testFile = "file:///data/data/com.breautek.fuse.filesystem.test/files/truncateTest1";

            FuseTestAPIClient client;
            try {
                byte[] content = createParamsBuffer(testFile);
                client = new FuseTestAPIClient.Builder()
                        .setFuseContext(activity.getFuseContext())
                        .setAPIPort(port)
                        .setAPISecret(secret)
                        .setPluginID("FuseFilesystem")
                        .setType("application/octet-stream")
                        .setEndpoint("/file/truncate")
                        .setContent(content)
                        .build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            FuseTestAPIClient.FuseAPITestResponse response = client.execute();
            assertEquals(200, response.getStatus());

            File file = new File(testFile);

            assertEquals(0, file.length());
        });
    }

    @Test
    public void canTruncateFileWithNewContent() {
        activityRule.getScenario().onActivity(activity -> {
            int port = activity.getFuseContext().getAPIPort();
            String secret = activity.getFuseContext().getAPISecret();

            String testFile = "file:///data/data/com.breautek.fuse.filesystem.test/files/truncateTest1";

            byte [] newContent = "new content".getBytes();

            FuseTestAPIClient client;
            try {
                byte[] content = createParamsBuffer(testFile, newContent);
                client = new FuseTestAPIClient.Builder()
                        .setFuseContext(activity.getFuseContext())
                        .setAPIPort(port)
                        .setAPISecret(secret)
                        .setPluginID("FuseFilesystem")
                        .setType("application/octet-stream")
                        .setEndpoint("/file/truncate")
                        .setContent(content)
                        .build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            FuseTestAPIClient.FuseAPITestResponse response = client.execute();
            assertEquals(200, response.getStatus());

            File file = new File(testFile);

            assertEquals(newContent.length, file.length());

            String newContentStr = null;
            FileReader reader = null;
            try {
                reader = new FileReader(file);
                char[] readerBuffer = new char[newContent.length];
                reader.read(readerBuffer);
                newContentStr = new String(readerBuffer);
                reader.close();
            } catch (Exception e) {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                throw new RuntimeException(e);
            }

            assertEquals("new content", newContentStr);
        });
    }

    @Test
    public void canAppendDataToFile() {
        activityRule.getScenario().onActivity(activity -> {
            int port = activity.getFuseContext().getAPIPort();
            String secret = activity.getFuseContext().getAPISecret();

            String testFile = "file:///data/data/com.breautek.fuse.filesystem.test/files/appendFileTest";

            byte [] newContent = " + more data!".getBytes();

            FuseTestAPIClient client;
            try {
                byte[] content = createParamsBuffer(testFile, newContent);
                client = new FuseTestAPIClient.Builder()
                        .setFuseContext(activity.getFuseContext())
                        .setAPIPort(port)
                        .setAPISecret(secret)
                        .setPluginID("FuseFilesystem")
                        .setType("application/octet-stream")
                        .setEndpoint("/file/append")
                        .setContent(content)
                        .build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            FuseTestAPIClient.FuseAPITestResponse response = client.execute();
            assertEquals(200, response.getStatus());

            int reportedBytesWritten = Integer.parseInt(response.readAsString());

            assertEquals(newContent.length, reportedBytesWritten);

            File file = new File(testFile);

            String newContentStr = null;
            FileReader reader = null;
            try {
                reader = new FileReader(file);
                char[] readerBuffer = new char[(int) file.length()];
                reader.read(readerBuffer);
                newContentStr = new String(readerBuffer);
                reader.close();
            } catch (Exception e) {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                throw new RuntimeException(e);
            }

            assertEquals("Initial State! + more data!", newContentStr);
        });
    }

    @Test
    public void canWriteDataToFile() {
        activityRule.getScenario().onActivity(activity -> {
            int port = activity.getFuseContext().getAPIPort();
            String secret = activity.getFuseContext().getAPISecret();

            String testFile = "file:///data/data/com.breautek.fuse.filesystem.test/files/writeFileTest";

            JSONObject jparams = new JSONObject();
            try {
                jparams.put("path", testFile);
                jparams.put("offset", 0);
            }
            catch (JSONException e) {
                throw new RuntimeException(e);
            }

            byte [] newContent = "Rewrite".getBytes();

            FuseTestAPIClient client;
            try {
                byte[] content = createParamsBuffer(jparams.toString(), newContent);
                client = new FuseTestAPIClient.Builder()
                        .setFuseContext(activity.getFuseContext())
                        .setAPIPort(port)
                        .setAPISecret(secret)
                        .setPluginID("FuseFilesystem")
                        .setType("application/octet-stream")
                        .setEndpoint("/file/write")
                        .setContent(content)
                        .build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            FuseTestAPIClient.FuseAPITestResponse response = client.execute();
            assertEquals(200, response.getStatus());

            int reportedBytesWritten = Integer.parseInt(response.readAsString());

            assertEquals(newContent.length, reportedBytesWritten);

            File file = new File(testFile);

            String newContentStr = null;
            FileReader reader = null;
            try {
                reader = new FileReader(file);
                char[] readerBuffer = new char[(int) file.length()];
                reader.read(readerBuffer);
                newContentStr = new String(readerBuffer);
                reader.close();
            } catch (Exception e) {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                throw new RuntimeException(e);
            }

            assertEquals("Rewrite State!", newContentStr);
        });
    }

    @Test
    public void canWriteDataToFileWithOffset() {
        activityRule.getScenario().onActivity(activity -> {
            int port = activity.getFuseContext().getAPIPort();
            String secret = activity.getFuseContext().getAPISecret();

            String testFile = "file:///data/data/com.breautek.fuse.filesystem.test/files/writeFileTestWithOffset";

            JSONObject jparams = new JSONObject();
            try {
                jparams.put("path", testFile);
                jparams.put("offset", 2);
            }
            catch (JSONException e) {
                throw new RuntimeException(e);
            }

            byte [] newContent = "Rewrite".getBytes();

            FuseTestAPIClient client;
            try {
                byte[] content = createParamsBuffer(jparams.toString(), newContent);
                client = new FuseTestAPIClient.Builder()
                        .setFuseContext(activity.getFuseContext())
                        .setAPIPort(port)
                        .setAPISecret(secret)
                        .setPluginID("FuseFilesystem")
                        .setType("application/octet-stream")
                        .setEndpoint("/file/write")
                        .setContent(content)
                        .build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            FuseTestAPIClient.FuseAPITestResponse response = client.execute();
            assertEquals(200, response.getStatus());

            int reportedBytesWritten = Integer.parseInt(response.readAsString());

            assertEquals(newContent.length, reportedBytesWritten);

            File file = new File(testFile);

            String newContentStr = null;
            FileReader reader = null;
            try {
                reader = new FileReader(file);
                char[] readerBuffer = new char[(int) file.length()];
                reader.read(readerBuffer);
                newContentStr = new String(readerBuffer);
                reader.close();
            } catch (Exception e) {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                throw new RuntimeException(e);
            }

            assertEquals("InRewritetate!", newContentStr);
        });
    }

    @Test
    public void canDeleteFile() {
        activityRule.getScenario().onActivity(activity -> {
            int port = activity.getFuseContext().getAPIPort();
            String secret = activity.getFuseContext().getAPISecret();

            String testFile = "file:///data/data/com.breautek.fuse.filesystem.test/files/removeFileTest";

            FuseTestAPIClient client;
            try {
                JSONObject params = new JSONObject();
                params.put("path", testFile);
                params.put("recursive", false);

                client = new FuseTestAPIClient.Builder()
                        .setFuseContext(activity.getFuseContext())
                        .setAPIPort(port)
                        .setAPISecret(secret)
                        .setPluginID("FuseFilesystem")
                        .setType("application/octet-stream")
                        .setEndpoint("/file/remove")
                        .setContent(params.toString())
                        .build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            FuseTestAPIClient.FuseAPITestResponse response = client.execute();

            assertEquals(200, response.getStatus());

            String result = response.readAsString();

            assertEquals("true", result);

            File file = new File(testFile);

            assertFalse(file.exists());
        });
    }

    @Test
    public void deleteAPIShouldReturnFalse() {
        activityRule.getScenario().onActivity(activity -> {
            int port = activity.getFuseContext().getAPIPort();
            String secret = activity.getFuseContext().getAPISecret();

            String testFile = "file:///data/data/com.breautek.fuse.filesystem.test/files/doesNotExists";

            FuseTestAPIClient client;
            try {
                JSONObject params = new JSONObject();
                params.put("path", testFile);
                params.put("recursive", false);

                client = new FuseTestAPIClient.Builder()
                        .setFuseContext(activity.getFuseContext())
                        .setAPIPort(port)
                        .setAPISecret(secret)
                        .setPluginID("FuseFilesystem")
                        .setType("application/octet-stream")
                        .setEndpoint("/file/remove")
                        .setContent(params.toString())
                        .build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            FuseTestAPIClient.FuseAPITestResponse response = client.execute();

            assertEquals(200, response.getStatus());

            String result = response.readAsString();

            assertEquals("false", result);
        });
    }

    @Test
    public void canRecursivelyDelete() {
        activityRule.getScenario().onActivity(activity -> {
            int port = activity.getFuseContext().getAPIPort();
            String secret = activity.getFuseContext().getAPISecret();

            String testFile = "file:///data/data/com.breautek.fuse.filesystem.test/files/removeRecursiveTest/abc/def";

            FuseTestAPIClient client;
            try {
                JSONObject params = new JSONObject();
                params.put("path", testFile);
                params.put("recursive", true);

                client = new FuseTestAPIClient.Builder()
                        .setFuseContext(activity.getFuseContext())
                        .setAPIPort(port)
                        .setAPISecret(secret)
                        .setPluginID("FuseFilesystem")
                        .setType("application/octet-stream")
                        .setEndpoint("/file/remove")
                        .setContent(params.toString())
                        .build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            FuseTestAPIClient.FuseAPITestResponse response = client.execute();

            assertEquals(200, response.getStatus());

            String result = response.readAsString();

            assertEquals("true", result);

            File file = new File(testFile);

            assertFalse(file.exists());
        });
    }

    @Test
    public void existsShouldBeTrue() {
        activityRule.getScenario().onActivity(activity -> {
            int port = activity.getFuseContext().getAPIPort();
            String secret = activity.getFuseContext().getAPISecret();

            String testFile = "file:///data/data/com.breautek.fuse.filesystem.test/files/appendFileTest";

            FuseTestAPIClient client;
            try {
                client = new FuseTestAPIClient.Builder()
                        .setFuseContext(activity.getFuseContext())
                        .setAPIPort(port)
                        .setAPISecret(secret)
                        .setPluginID("FuseFilesystem")
                        .setType("application/octet-stream")
                        .setEndpoint("/file/exists")
                        .setContent(testFile)
                        .build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            FuseTestAPIClient.FuseAPITestResponse response = client.execute();

            assertEquals(200, response.getStatus());

            String result = response.readAsString();

            assertEquals("true", result);

            File file = new File(testFile);
            assertTrue(file.exists());
        });
    }

    @Test
    public void existsShouldBeFalse() {
        activityRule.getScenario().onActivity(activity -> {
            int port = activity.getFuseContext().getAPIPort();
            String secret = activity.getFuseContext().getAPISecret();

            String testFile = "file:///data/data/com.breautek.fuse.filesystem.test/files/doesNotExists";

            FuseTestAPIClient client;
            try {
                client = new FuseTestAPIClient.Builder()
                        .setFuseContext(activity.getFuseContext())
                        .setAPIPort(port)
                        .setAPISecret(secret)
                        .setPluginID("FuseFilesystem")
                        .setType("application/octet-stream")
                        .setEndpoint("/file/exists")
                        .setContent(testFile)
                        .build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            FuseTestAPIClient.FuseAPITestResponse response = client.execute();

            assertEquals(200, response.getStatus());

            String result = response.readAsString();

            assertEquals("false", result);

            File file = new File(testFile);
            assertFalse(file.exists());
        });
    }
}
