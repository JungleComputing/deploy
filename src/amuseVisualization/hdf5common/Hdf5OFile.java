package amuseVisualization.hdf5common;

import ncsa.hdf.object.FileFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hdf5OFile {
    private static final Logger logger = LoggerFactory.getLogger(Hdf5OFile.class);
    private final String fileName;
    private boolean readOnly = false;
    FileFormat file = null;

    public Hdf5OFile(String fileName) {
        this.fileName = fileName;
    }

    public void open(boolean readOnly) throws Hdf5FileOpenFailedException {
        FileFormat fileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);
        this.readOnly = readOnly;

        try {
            if (readOnly) {
                file = fileFormat.createInstance(fileName, FileFormat.READ);
            } else {
                file = fileFormat.createInstance(fileName, FileFormat.WRITE);
            }
            file.open();
        } catch (Exception e) {
            throw new Hdf5FileOpenFailedException(e.getMessage());
        }
    }

    public void create() throws Hdf5FileCreationFailedException {
        FileFormat fileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);

        try {
            file = fileFormat.createInstance(fileName, FileFormat.CREATE);
            file.open();
        } catch (Exception e) {
            throw new Hdf5FileCreationFailedException(e.getMessage());
        }
    }

    public void close() throws Hdf5FileNotOpenException {
        try {
            file.close();
        } catch (Exception e) {
            throw new Hdf5FileNotOpenException(e.getMessage());
        }
    }

    public Hdf5OGroup getRoot() {
        return (Hdf5OGroup) ((javax.swing.tree.DefaultMutableTreeNode) file.getRootNode()).getUserObject();
    }

    public String getName() {
        return fileName;
    }
}
