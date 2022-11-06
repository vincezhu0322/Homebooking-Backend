package com.laioffer.homebooking.repository;

import com.laioffer.homebooking.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.laioffer.homebooking.model.Stay;
import com.laioffer.homebooking.model.User;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
   List<Reservation> findByGuest(User guest);

   List<Reservation> findByStay(Stay stay);

   Reservation findByIdAndGuest(Long id, User guest); // for deletion

   List<Reservation> findByStayAndCheckoutDateAfter(Stay stay, LocalDate date);
}
