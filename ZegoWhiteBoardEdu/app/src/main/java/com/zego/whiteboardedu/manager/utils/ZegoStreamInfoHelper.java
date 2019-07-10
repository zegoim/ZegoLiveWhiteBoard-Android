package com.zego.whiteboardedu.manager.utils;

import android.support.annotation.NonNull;

import com.zego.zegoliveroom.entity.ZegoStreamInfo;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public final class ZegoStreamInfoHelper {

    /**
     * 判断两条流是否一致，只对streamID进行判断
     */
    public static boolean streamEquals(ZegoStreamInfo stream, Object obj) {
        return obj instanceof ZegoStreamInfo && stream.streamID.equals(((ZegoStreamInfo) obj).streamID);
    }

    public static boolean isContainsStream(@NonNull Collection<ZegoStreamInfo> streams, ZegoStreamInfo stream) {
        for (ZegoStreamInfo s : streams) {
            if (streamEquals(s, stream)) {
                return true;
            }
        }
        return false;
    }

    public static boolean addStreamToSet(@NonNull Set<ZegoStreamInfo> streams, ZegoStreamInfo stream) {
        if (isContainsStream(streams, stream)) {
            return false;
        } else {
            streams.add(stream);
            return true;
        }
    }

    public static boolean addAllStreamToSet(@NonNull Set<ZegoStreamInfo> streams, Collection<ZegoStreamInfo> streamsToAdd) {
        boolean modified = false;
        for (ZegoStreamInfo stream : streamsToAdd) {
            modified |= addStreamToSet(streams, stream);
        }
        return modified;
    }

    public static boolean removeStreamFromCollection(@NonNull Collection<ZegoStreamInfo> streams, ZegoStreamInfo deleteStream) {
        boolean modified = false;

        for (Iterator<ZegoStreamInfo> i = streams.iterator(); i.hasNext(); ) {
            if (streamEquals(i.next(), deleteStream)) {
                i.remove();
                modified = true;
            }
        }
        return modified;
    }

    public static boolean removeAllStreamFromCollection(@NonNull Collection<ZegoStreamInfo> streams, @NonNull Collection<ZegoStreamInfo> deleteStreams) {
        boolean modified = false;

        for (Iterator<ZegoStreamInfo> i = streams.iterator(); i.hasNext(); ) {
            if (isContainsStream(deleteStreams, i.next())) {
                i.remove();
                modified = true;
            }
        }
        return modified;
    }

    public static boolean retainAllStreamFromCollection(@NonNull Collection<ZegoStreamInfo> streams, @NonNull Collection<ZegoStreamInfo> containStreams) {
        boolean modified = false;
        Iterator<ZegoStreamInfo> it = streams.iterator();
        while (it.hasNext()) {
            if (!isContainsStream(containStreams, it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }

    public static String streamToString(ZegoStreamInfo stream) {
        return "ZegoStreamInfo{" +
                "userID='" + stream.userID + '\'' +
                ", userName='" + stream.userName + '\'' +
                ", streamID='" + stream.streamID + '\'' +
                ", extraInfo='" + stream.extraInfo + '\'' +
                '}';
    }
}
