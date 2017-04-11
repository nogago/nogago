/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Users\\Ulrich Ehret\\Apps-Code\\Tracks\\MyTracksLib\\src\\com\\google\\android\\apps\\mytracks\\services\\ITrackRecordingService.aidl
 */
package com.google.android.apps.mytracks.services;
/**
 * MyTracks service.
 * This service is the process that actually records and manages tracks.
 */
public interface ITrackRecordingService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.google.android.apps.mytracks.services.ITrackRecordingService
{
private static final java.lang.String DESCRIPTOR = "com.google.android.apps.mytracks.services.ITrackRecordingService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.google.android.apps.mytracks.services.ITrackRecordingService interface,
 * generating a proxy if needed.
 */
public static com.google.android.apps.mytracks.services.ITrackRecordingService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.google.android.apps.mytracks.services.ITrackRecordingService))) {
return ((com.google.android.apps.mytracks.services.ITrackRecordingService)iin);
}
return new com.google.android.apps.mytracks.services.ITrackRecordingService.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_startNewTrack:
{
data.enforceInterface(DESCRIPTOR);
long _result = this.startNewTrack();
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_isRecording:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isRecording();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_isStartNewRecording:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isStartNewRecording();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getRecordingTrackId:
{
data.enforceInterface(DESCRIPTOR);
long _result = this.getRecordingTrackId();
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_insertWaypoint:
{
data.enforceInterface(DESCRIPTOR);
com.google.android.apps.mytracks.content.WaypointCreationRequest _arg0;
if ((0!=data.readInt())) {
_arg0 = com.google.android.apps.mytracks.content.WaypointCreationRequest.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
long _result = this.insertWaypoint(_arg0);
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_recordLocation:
{
data.enforceInterface(DESCRIPTOR);
android.location.Location _arg0;
if ((0!=data.readInt())) {
_arg0 = android.location.Location.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.recordLocation(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_endCurrentTrack:
{
data.enforceInterface(DESCRIPTOR);
this.endCurrentTrack();
reply.writeNoException();
return true;
}
case TRANSACTION_getSensorData:
{
data.enforceInterface(DESCRIPTOR);
byte[] _result = this.getSensorData();
reply.writeNoException();
reply.writeByteArray(_result);
return true;
}
case TRANSACTION_getSensorState:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getSensorState();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.google.android.apps.mytracks.services.ITrackRecordingService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
/**
   * Starts recording a new track.
   *
   * @return the track ID of the new track
   */
@Override public long startNewTrack() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_startNewTrack, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
   * Checks and returns whether we're currently recording a track.
   */
@Override public boolean isRecording() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isRecording, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
   * Checks and returns whether we're currently recording a track.
   */
@Override public boolean isStartNewRecording() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isStartNewRecording, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
   * Returns the track ID of the track currently being recorded, or -1 if none
   * is being recorded. This ID can then be used to read track data from the
   * content source.
   */
@Override public long getRecordingTrackId() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getRecordingTrackId, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
   * Inserts a waypoint marker in the track being recorded.
   *
   * @param request Details for the waypoint to be inserted.
   * @return the unique ID of the inserted marker
   */
@Override public long insertWaypoint(com.google.android.apps.mytracks.content.WaypointCreationRequest request) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((request!=null)) {
_data.writeInt(1);
request.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_insertWaypoint, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
   * Inserts a location in the track being recorded.
   *
   * When recording, locations detected by the GPS are already automatically
   * added to the track, so this should be used only for adding special points
   * or for testing.
   *
   * @param loc the location to insert
   */
@Override public void recordLocation(android.location.Location loc) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((loc!=null)) {
_data.writeInt(1);
loc.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_recordLocation, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
   * Stops recording the current track.
   */
@Override public void endCurrentTrack() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_endCurrentTrack, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
   * The current sensor data.
   * The data is returned as a byte array which is a binary version of a
   * Sensor.SensorDataSet object.
   * @return the current sensor data or null if there is none.
   */
@Override public byte[] getSensorData() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
byte[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getSensorData, _data, _reply, 0);
_reply.readException();
_result = _reply.createByteArray();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
   * The current state of the sensor manager.
   * The value is the value of a Sensor.SensorState enum.
   */
@Override public int getSensorState() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getSensorState, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_startNewTrack = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_isRecording = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_isStartNewRecording = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_getRecordingTrackId = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_insertWaypoint = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_recordLocation = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_endCurrentTrack = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_getSensorData = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_getSensorState = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
}
/**
   * Starts recording a new track.
   *
   * @return the track ID of the new track
   */
public long startNewTrack() throws android.os.RemoteException;
/**
   * Checks and returns whether we're currently recording a track.
   */
public boolean isRecording() throws android.os.RemoteException;
/**
   * Checks and returns whether we're currently recording a track.
   */
public boolean isStartNewRecording() throws android.os.RemoteException;
/**
   * Returns the track ID of the track currently being recorded, or -1 if none
   * is being recorded. This ID can then be used to read track data from the
   * content source.
   */
public long getRecordingTrackId() throws android.os.RemoteException;
/**
   * Inserts a waypoint marker in the track being recorded.
   *
   * @param request Details for the waypoint to be inserted.
   * @return the unique ID of the inserted marker
   */
public long insertWaypoint(com.google.android.apps.mytracks.content.WaypointCreationRequest request) throws android.os.RemoteException;
/**
   * Inserts a location in the track being recorded.
   *
   * When recording, locations detected by the GPS are already automatically
   * added to the track, so this should be used only for adding special points
   * or for testing.
   *
   * @param loc the location to insert
   */
public void recordLocation(android.location.Location loc) throws android.os.RemoteException;
/**
   * Stops recording the current track.
   */
public void endCurrentTrack() throws android.os.RemoteException;
/**
   * The current sensor data.
   * The data is returned as a byte array which is a binary version of a
   * Sensor.SensorDataSet object.
   * @return the current sensor data or null if there is none.
   */
public byte[] getSensorData() throws android.os.RemoteException;
/**
   * The current state of the sensor manager.
   * The value is the value of a Sensor.SensorState enum.
   */
public int getSensorState() throws android.os.RemoteException;
}
