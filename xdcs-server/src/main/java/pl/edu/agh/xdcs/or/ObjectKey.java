package pl.edu.agh.xdcs.or;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static lombok.AccessLevel.PRIVATE;

/**
 * @author Kamil Jarosz
 */
@Getter
@AllArgsConstructor(access = PRIVATE)
public class ObjectKey {
    private String objectId;
    private Class<? extends ObjectBase> type;

    public static ObjectKey from(String objectId, Class<? extends ObjectBase> type) {
        return new ObjectKey(objectId, type);
    }
}
