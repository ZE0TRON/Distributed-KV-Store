package de.tum.i13.server.kv;

public interface KVClientMessage {

     enum StatusType {
         GET, 			/* Get - request */
         GET_ERROR, 		/* requested tuple (i.e. value) not found */
         GET_SUCCESS, 	/* requested tuple (i.e. value) found */
         PUT, 			/* Put - request */
         PUT_SUCCESS, 	/* Put - request successful, tuple inserted */
         PUT_UPDATE, 	/* Put - request successful, i.e. value updated */
         PUT_ERROR, 		/* Put - request not successful */
         DELETE, 		/* Delete - request */
         DELETE_SUCCESS, /* Delete - request successful */
         DELETE_ERROR, 	/* Delete - request successful */
         ERROR,
         CONNECTION_ESTABLISHED,
         SERVER_STOPPED,
         SERVER_NOT_RESPONSIBLE,
         SERVER_WRITE_LOCK,
         KEYRANGE_SUCCESS,
         KEYRANGE_READ_SUCCESS,
         SUBSCRIBE_SUCCESS,
         SUBSCRIBE_ERROR,
         UNSUBSCRIBE_SUCCESS,
         UNSUBSCRIBE_ERROR,
         NOTIFICATION_UPDATE,
         NOTIFICATION_DELETE
    }

    /**
     * @return the key that is associated with this message,
     * null if not key is associated.
     */
    String getKey();

    /**
     * @return the value that is associated with this message,
     * null if not value is associated.
     */
    String getValue();

    /**
     * @return a status string that is used to identify request types,
     * response types and error types associated to the message.
     */
    StatusType getStatus();

}
