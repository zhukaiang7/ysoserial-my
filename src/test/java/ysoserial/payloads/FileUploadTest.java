package ysoserial.payloads;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Callable;

import org.junit.Assert;

import com.google.common.io.Files;

import ysoserial.CustomTest;
import ysoserial.util.OS;

/**
 * @author mbechler
 *
 */
public class FileUploadTest implements CustomTest {

    /**
     *
     */
    private static final byte[] FDATA = new byte[] {(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF };
    private File source;
    private File repo;


    /**
     *
     */
    public FileUploadTest () {
        try {
            source = File.createTempFile("fut", "-source");
            repo = Files.createTempDir();
        }
        catch ( IOException e ) {
            e.printStackTrace();
        }
    }


    public synchronized void run ( Callable<Object> payload ) throws Exception {
        try {
            Files.write(FDATA, this.source);
            Assert.assertTrue(this.source.exists());
            payload.call();

            File found = null;
            for ( File f : this.repo.listFiles()) {
                found = f;
                break;
            }
            Assert.assertNotNull("File not copied", found);
            if (OS.get() != OS.WINDOWS) {
                // windows' file locking seems to cause this to fail
                Assert.assertFalse("Source not deleted", this.source.exists());
            }
            Assert.assertTrue("Contents not copied", Arrays.equals(FDATA, Files.toByteArray(found)));
        } finally {
            if ( this.repo.exists()) {
                for ( File f : this.repo.listFiles()) {
                    f.deleteOnExit();
                }
                this.repo.deleteOnExit();
            }
            this.source.deleteOnExit();
        }
    }

    public String getPayloadArgs () {
        return "copyAndDelete;" + this.source.getAbsolutePath() + ";" + this.repo.getAbsolutePath();
    }

}
