package com.laioffer.homebooking.exception;

public class ReservationCollisionException extends RuntimeException {
   public ReservationCollisionException(String message) {
      super(message);
   }
}
