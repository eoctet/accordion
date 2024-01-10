package chat.octet.accordion.core.entity;


import lombok.Getter;

/**
 * Accordion session, which contains the execution status and some global parameters.
 *
 * @author <a href="https://github.com/eoctet">William</a>
 */
public class Session {
    private final static String SESSION_GLOBAL_PARAMETER = "SESSION_GLOBAL_PARAMETER";
    private final Tuple<String, Object> data;
    @Getter
    private final Tuple<String, Object> global;

    public Session() {
        this.data = new Tuple<>();
        this.global = new Tuple<>();
        this.data.put(SESSION_GLOBAL_PARAMETER, global);
    }

    public boolean containsKey(String key) {
        return this.data.containsKey(key);
    }

    public Session add(String key, Object value) {
        return add(key, value, false);
    }

    public Session add(String key, Object value, boolean global) {
        if (global) {
            this.global.put(key, value);
        } else {
            this.data.put(key, value);
        }
        return this;
    }

    public void remove(String key) {
        this.data.remove(key);
    }

    public Object getValue(String key) {
        return this.data.get(key);
    }

    public <T> T getValue(String key, Class<T> clazz) {
        return clazz.cast(this.data.get(key));
    }

    public void clear() {
        this.data.clear();
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
