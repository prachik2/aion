package org.aion.db.impl.lmdb;

import org.aion.base.util.ByteArrayWrapper;
import org.aion.db.impl.AbstractDB;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.nio.ByteBuffer.allocateDirect;
import static org.lmdbjava.DbiFlags.MDB_CREATE;
import static org.lmdbjava.Env.create;

public class LMDBWrapper extends AbstractDB {

    private Dbi<ByteBuffer> db;
    private Env<ByteBuffer> env;

    public LMDBWrapper(String name, String path, boolean enableCache, boolean enableCompression) {
        super(name, path, enableCache, enableCompression);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ":" + propertiesInfo();
    }

    @Override
    public boolean open() {
        if (isOpen()) {
            return true;
        }

        LOG.debug("init database {}", this.toString());

        File f = new File(path);
        File dbRoot = f.getParentFile();

        // make the parent directory if not exists
        if (!dbRoot.exists()) {
            if (!f.getParentFile().mkdirs()) {
                LOG.error("Failed to initialize the database storage for " + this.toString() + ".");
                return false;
            }
        }

        env = create().setMapSize(10_485_760).setMaxDbs(1).open(f);

        try {
            db = env.openDbi(name, MDB_CREATE);
        } catch (Exception e1) {
            LOG.error("Failed to open the database " + this.toString() + " due to: ", e1);
            if (e1.getMessage().contains("No space left on device")) {
                LOG.error("Shutdown due to lack of disk space.");
                System.exit(0);
            }

            // close the connection and cleanup if needed
            close();
        }

        return isOpen();
    }

    @Override
    public void close() {
        // do nothing if already closed
        if (db == null) {
            return;
        }

        LOG.info("Closing database " + this.toString());

        try {
            // attempt to close the database
            db.close();
        } catch (Exception e) {
            LOG.error("Failed to close the database " + this.toString() + ".", e);
        } finally {
            // ensuring the db is null after close was called
            db = null;
        }
    }

    @Override
    public boolean commit() {
        return false;
    }

    @Override
    public void compact() {

    }

    @Override
    public void drop() {

    }

    @Override
    public Optional<String> getName() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getPath() {
        return Optional.empty();
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public boolean isLocked() {
        return false;
    }

    @Override
    public boolean commitCache(Map<ByteArrayWrapper, byte[]> cache) {
        return false;
    }

    @Override
    public boolean isAutoCommitEnabled() {
        return false;
    }

    @Override
    public boolean isPersistent() {
        return false;
    }

    @Override
    public boolean isCreatedOnDisk() {
        return false;
    }

    @Override
    public long approximateSize() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Set<byte[]> keys() {
        return null;
    }

    @Override
    protected byte[] getInternal(byte[] k) {
        ByteBuffer value;
        ByteBuffer key = allocateDirect(env.getMaxKeySize());
        key.put(k).flip();

        try (Txn<ByteBuffer> rtx = env.txnRead()) {
            value = db.get(rtx, key);
        }

        return value.array();
    }

    @Override
    public void put(byte[] k, byte[] v) {
        check(k);
        check();

        final ByteBuffer key = allocateDirect(env.getMaxKeySize());
        key.put(k).flip();

        if (v == null) {
            db.delete(key);
        } else {
            final ByteBuffer val = allocateDirect(700);
            val.put(v).flip();
            db.put(key, val);
        }
    }

    @Override
    public void delete(byte[] k) {
        check(k);
        check();

        final ByteBuffer key = allocateDirect(env.getMaxKeySize());

        key.put(k).flip();
        db.delete(key);
    }

    @Override
    public void putBatch(Map<byte[], byte[]> inputMap) {

    }

    @Override
    public void putToBatch(byte[] key, byte[] value) {

    }

    @Override
    public void commitBatch() {

    }

    @Override
    public void deleteBatch(Collection<byte[]> keys) {

    }
}
