package app.devlife.connect2sql.ui.connection;

import app.devlife.connect2sql.db.model.connection.ConnectionInfo;
import app.devlife.connect2sql.sql.DriverType;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class ConnectionInfoEditorRequest implements Parcelable {

    private final Action mAction;
    private long mConnectionInfoId;
    private DriverType mDriverType;

    protected ConnectionInfoEditorRequest(Action action, DriverType driverType,
            long connectionInfoId) {
        super();
        mAction = action;
        mDriverType = driverType;
        mConnectionInfoId = connectionInfoId;
    }

    /**
     * Constructor for a {@link Action#NEW} {@link ConnectionInfo}
     */
    public ConnectionInfoEditorRequest(DriverType driverType) {
        this(Action.NEW, driverType, 0);
    }

    /**
     * Constructor for an {@link Action#EDIT} of a {@link ConnectionInfo}
     *
     * @param connectionInfoId
     */
    public ConnectionInfoEditorRequest(long connectionInfoId) {
        this(Action.EDIT, null, connectionInfoId);
    }

    @NonNull
    public Action getAction() {
        return mAction;
    }

    public DriverType getDriverType() {
        return mDriverType;
    }

    public void setDriverType(DriverType driverType) {
        mDriverType = driverType;
    }

    public long getConnectionInfoId() {
        return mConnectionInfoId;
    }

    public static enum Action {
        NEW, EDIT
    }

    /* ******************
     * Parcelable interface
     */

    protected ConnectionInfoEditorRequest(Parcel in) {
        mAction = Action.valueOf(in.readString());
        String driverTypeString = in.readString();
        mDriverType = driverTypeString == null ? null : DriverType.valueOf(driverTypeString);
        mConnectionInfoId = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mAction.name());
        dest.writeString(mDriverType == null ? null : mDriverType.name());
        dest.writeLong(mConnectionInfoId);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ConnectionInfoEditorRequest> CREATOR = new Parcelable.Creator<ConnectionInfoEditorRequest>() {
        @Override
        public ConnectionInfoEditorRequest createFromParcel(Parcel in) {
            return new ConnectionInfoEditorRequest(in);
        }

        @Override
        public ConnectionInfoEditorRequest[] newArray(int size) {
            return new ConnectionInfoEditorRequest[size];
        }
    };
}
