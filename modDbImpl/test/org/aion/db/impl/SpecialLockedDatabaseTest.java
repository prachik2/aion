package org.aion.db.impl;

import org.aion.base.db.IByteArrayKeyValueDatabase;
import org.aion.db.impl.DatabaseFactory;
import org.aion.db.generic.SpecialLockedDatabase;

import java.util.Random;

import org.aion.db.impl.leveldb.LevelDBConstants;
import org.junit.Test;

import java.io.File;
import java.util.Properties;

import static java.lang.Thread.sleep;
import static org.aion.db.impl.DatabaseFactory.Props;
import static org.junit.Assert.*;

public class SpecialLockedDatabaseTest {

    public String dbPath = new File(System.getProperty("user.dir"), "tmp").getAbsolutePath();
    public String dbVendor = DBVendor.LEVELDB.toValue();
    public String dbName = "test";

    private DBVendor driver = DBVendor.LEVELDB;

    private SpecialLockedDatabase specialLockedDatabase;

    public void generateDatabase() {

        Properties props = new Properties();
        props.setProperty(Props.DB_TYPE, dbVendor);
        props.setProperty(Props.DB_NAME, dbName);
        props.setProperty(Props.DB_PATH, dbPath);
        props.setProperty(Props.BLOCK_SIZE, String.valueOf(LevelDBConstants.BLOCK_SIZE));
        props.setProperty(Props.MAX_FD_ALLOC, String.valueOf(LevelDBConstants.MAX_OPEN_FILES));
        props.setProperty(Props.WRITE_BUFFER_SIZE, String.valueOf(LevelDBConstants.WRITE_BUFFER_SIZE));
        props.setProperty(Props.DB_CACHE_SIZE, String.valueOf(LevelDBConstants.CACHE_SIZE));

        IByteArrayKeyValueDatabase db = DatabaseFactory.connect(props);
        assertNotNull(db);
        specialLockedDatabase = new SpecialLockedDatabase(db);
    }


    @Test
    public void testThreadForSpecialLockedDatabase() {
        generateDatabase();
        specialLockedDatabase.open();

        TestThread testThread_1 = new TestThread();
        Thread thread_1 = new Thread(testThread_1);

        TestThread testThread_2 = new TestThread();
        Thread thread_2 = new Thread(testThread_2);
        /*TestThread testThread_3 = new TestThread();
        Thread thread_3 = new Thread(testThread_3);
        TestThread testThread_4 = new TestThread();
        Thread thread_4 = new Thread(testThread_4);*/

        thread_1.start();
        thread_2.start();
        //thread_3.run();
        //thread_4.run();
        try{
            Thread.sleep(10000);
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    class TestThread implements Runnable {



        @Override
        public void run() {
            Random random = new Random();

            for (int i = 1; i <= 10; i++) {
                byte[] key = intToBytes(i);
                byte[] value = intToBytes(random.nextInt());
                byte[] result;

                specialLockedDatabase.put(key, value);
                System.out.println("Thread" + this.toString() + "puts" + key + "  " + value);

                result = specialLockedDatabase.get(key).get();
                System.out.println("Thread" + this.toString() + "gets" + key + "  " + value);

                assertArrayEquals(value, result);
            }
        }

        public byte[] intToBytes(int intData) {
            byte[] bytes = new byte[4];
            bytes[0] = (byte) (intData & 0xff);
            bytes[1] = (byte) ((intData >> 8) & 0xff);
            bytes[2] = (byte) ((intData >> 16) & 0xff);
            bytes[3] = (byte) ((intData >> 24) & 0xff);
            return bytes;
        }
    }

}

