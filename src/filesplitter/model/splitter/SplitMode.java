package filesplitter.model.splitter;

/**
 * SplitMode enumerator
 * @author Riccardo Rebottini
 */
public enum SplitMode {
    DEFAULT, ZIP, CRYPTED;

    @Override
    public String toString() {
        switch(this) {
            case DEFAULT: return "Split Only";
            case ZIP: return "Split & Zip";
            case CRYPTED:return "Split & Crypt";
        }
        return null;
    }
}