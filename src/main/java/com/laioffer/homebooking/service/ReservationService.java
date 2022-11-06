package com.laioffer.homebooking.service;

import com.laioffer.homebooking.exception.ReservationCollisionException;
import com.laioffer.homebooking.exception.ReservationNotFoundException;
import com.laioffer.homebooking.model.*;
import com.laioffer.homebooking.repository.ReservationRepository;
import com.laioffer.homebooking.repository.StayReservationDateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class ReservationService {
   private ReservationRepository reservationRepository;
   private StayReservationDateRepository stayReservationDateRepository;

   @Autowired
   public ReservationService(ReservationRepository reservationRepository, StayReservationDateRepository stayReservationDateRepository) {
      this.reservationRepository = reservationRepository;
      this.stayReservationDateRepository = stayReservationDateRepository;
   }

   public List<Reservation> listByGuest(String username) {
      return reservationRepository.findByGuest(new User.Builder().setUsername(username).build());
   }

   public List<Reservation> listByStay(Long stayId) {
      return reservationRepository.findByStay(new Stay.Builder().setId(stayId).build());
   }

   @Transactional(isolation = Isolation.SERIALIZABLE)
   public void add(Reservation reservation) {
      Set<Long> stayIds = stayReservationDateRepository.findByIdInAndDateBetween(
              Arrays.asList(reservation.getStay().getId()),
              reservation.getCheckinDate(),
              reservation.getCheckoutDate().minusDays(1)
      );
      if (!stayIds.isEmpty()) {
         throw new ReservationCollisionException("Already Reserved!");
      }
      List<StayReservedDate> dates = new ArrayList<>();
      for (LocalDate date = reservation.getCheckinDate(); date.isBefore(reservation.getCheckoutDate()); date = date.plusDays(1)) {
         dates.add(new StayReservedDate(new StayReservedDateKey(reservation.getStay().getId(), date), reservation.getStay()));
      }
      stayReservationDateRepository.saveAll(dates);
      reservationRepository.save(reservation);
   }

   @Transactional(isolation = Isolation.SERIALIZABLE)
   public void delete(Long reservationId, String username) {
      Reservation reservation = reservationRepository.findByIdAndGuest(reservationId, new User.Builder().setUsername(username).build());
      if (reservation == null) {
         throw new ReservationNotFoundException("Reservation is not available!");
      }
      reservationRepository.deleteById(reservationId);
      for (LocalDate date = reservation.getCheckinDate(); date.isBefore(reservation.getCheckinDate()); date.plusDays(1)){
         stayReservationDateRepository.deleteById(new StayReservedDateKey(reservation.getStay().getId(), date));
      }
   }
}
