package ibis.amuse.visualization.hdf5common;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

public class Hdf5File {
    // private static final Logger logger =
    // LoggerFactory.getLogger(Hdf5File.class);
    private final String fileName;
    private int file_id = -1;

    public Hdf5File(String fileName) {
        this.fileName = fileName;
    }

    public void open(boolean readOnly) throws Hdf5FileOpenFailedException {
        try {
            if (readOnly) {
                file_id = H5.H5Fopen(fileName, HDF5Constants.H5F_ACC_RDONLY,
                        HDF5Constants.H5P_DEFAULT);
            } else {
                file_id = H5.H5Fopen(fileName, HDF5Constants.H5F_ACC_RDWR,
                        HDF5Constants.H5P_DEFAULT);
            }
        } catch (HDF5LibraryException e) {
            throw new Hdf5FileOpenFailedException(e.getMessage());
        } catch (NullPointerException e) {
            throw new Hdf5FileOpenFailedException(e.getMessage());
        }
    }

    public void create() throws Hdf5FileCreationFailedException {
        try {
            file_id = H5.H5Fcreate(fileName, HDF5Constants.H5F_ACC_TRUNC,
                    HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);
        } catch (HDF5LibraryException e) {
            throw new Hdf5FileCreationFailedException(e.getMessage());
        } catch (NullPointerException e) {
            throw new Hdf5FileCreationFailedException(e.getMessage());
        }
    }

    public void close() throws Hdf5FileNotOpenException {
        try {
            H5.H5Fclose(file_id);
        } catch (HDF5LibraryException e) {
            throw new Hdf5FileNotOpenException(e.getMessage());
        }
    }

    public int getId() {
        return file_id;
    }

    public String getName() {
        return fileName;
    }
}
