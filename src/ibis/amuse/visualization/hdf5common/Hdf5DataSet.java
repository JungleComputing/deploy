package ibis.amuse.visualization.hdf5common;

import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.HDF5Constants;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;

public class Hdf5DataSet {
    // private static final Logger logger =
    // LoggerFactory.getLogger(Hdf5DataSet.class);
    private final String dataSetName;
    private final Hdf5File file;
    private int dataset_id;

    public Hdf5DataSet(Hdf5File file, String dataSetName) {
        this.file = file;
        this.dataSetName = dataSetName;
    }

    public void open() throws Hdf5FileNotOpenedException,
            Hdf5FileDoesntExistException, Hdf5DataSetNotFoundException {
        if (file.getId() >= 0) {
            try {
                dataset_id = H5.H5Dopen(file.getId(), "/" + dataSetName,
                        HDF5Constants.H5P_DEFAULT);
            } catch (HDF5LibraryException e) {
                throw new Hdf5DataSetNotFoundException(e.getMessage());
            }
        } else {
            try {
                String fileName = file.getName();
                throw new Hdf5FileNotOpenedException(fileName
                        + " was not opened before reading.");
            } catch (NullPointerException e) {
                throw new Hdf5FileDoesntExistException("file doesn't exist.");
            }
        }
    }

    // public read() {
    // try {
    // if (dataset_id >= 0) {
    // H5.H5Dread(dataset_id, HDF5Constants.H5T_NATIVE_INT,
    // HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL,
    // HDF5Constants.H5P_DEFAULT, dset_data);
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }
    //
    // public write() {
    // try {
    // if (dataset_id >= 0)
    // H5.H5Dwrite(dataset_id, HDF5Constants.H5T_NATIVE_INT,
    // HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL,
    // HDF5Constants.H5P_DEFAULT, dset_data);
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }

    public void close() {
        try {
            if (dataset_id >= 0)
                H5.H5Dclose(dataset_id);
        } catch (Exception e) {
            // logger.debug(e.getMessage());
        }
    }
}
