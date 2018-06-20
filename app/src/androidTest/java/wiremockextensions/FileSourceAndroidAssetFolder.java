package wiremockextensions;

import android.content.Context;

import com.github.tomakehurst.wiremock.common.BinaryFile;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.common.TextFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class FileSourceAndroidAssetFolder implements FileSource {
    private final Context context;
    private final String subDirectoryName;

    public FileSourceAndroidAssetFolder(Context context, String subDirectoryName) {

        this.context = context;
        this.subDirectoryName = subDirectoryName;
    }

    @Override
    public BinaryFile getBinaryFileNamed(String name) {
        TextFile textFileNamed = getTextFileNamed(name);
        return textFileNamed;
    }


    @Override
    public TextFile getTextFileNamed(String name) {
        try {
            InputStream inputStream = context.getAssets().open(this.subDirectoryName + "/" + name);
            return new BinaryFileInAndroidAssetFolder(this.subDirectoryName + "/" + name, inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void createIfNecessary() { }

    @Override
    public FileSource child(String subDirectoryName) {
        return new FileSourceAndroidAssetFolder(context, this.subDirectoryName + "/" + subDirectoryName.replaceAll("__", ""));
    }

    @Override
    public String getPath() {
        return context.getExternalCacheDir().getPath();
    }

    @Override
    public URI getUri() {
        return null;
    }

    @Override
    public List<TextFile> listFilesRecursively() {
        List<TextFile> list = new ArrayList<>();
        collectAssetFiles(subDirectoryName,list);
        return list;
    }

    //todo, so out of practice with my Lisp ive done this nasty none functional thing
    private boolean collectAssetFiles(String path, List<TextFile> collector) {

        String [] list;
        try {
            list = context.getAssets().list(path);
            if (list.length > 0) {
                // This is a folder
                for (String file : list) {
                    if (!collectAssetFiles(path + "/" + file, collector))
                        return false;
                    else {
                        TextFile textFile = null;
                        try {
                            textFile = new TextFile(new URI("asset://" +path + "/" + file));
                            collector.add(textFile);
                        } catch (URISyntaxException ignored) {
                            return false;
                        }
                    }
                }
            }
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    @Override
    public void writeTextFile(String name, String contents) { }

    @Override
    public void writeBinaryFile(String name, byte[] contents) { }

    @Override
    public boolean exists() {
        return true; //haha

    }

    @Override
    public void deleteFile(String name) { }
}
