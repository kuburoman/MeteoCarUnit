package cz.meteocar.unit.engine.network;

/**
 * Error codes used in communication with BoardUnit.
 */
public enum ErrorCodes {
    WRONG_USER_CREDENTIALS,
    WRONG_BOARD_UNIT,
    WRONG_USER_FOR_BOARD_UNIT,
    USER_NOT_FOUND,
    INVALID_JSON,
    BOARD_UNIT_IS_NOT_LINKED_WITH_CAR,
    NEWER_RECORDS_ARE_STORED,
    RECORDS_UPDATE_REQUIRED,
    WRONG_QUERY_PARAMETER,
    TRIP_DOESNT_EXIST
}
