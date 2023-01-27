package com.driver.services.impl;

import com.driver.model.TripBooking;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.model.Customer;
import com.driver.model.Driver;
import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;
import com.driver.model.TripStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		Customer customer=customerRepository2.findById(customerId).get();
         customerRepository2.delete(customer);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
         Driver bookdriver=new Driver();

		List<Driver> driverList=driverRepository2.findAll();

		int minId=Integer.MAX_VALUE;

		for(Driver driver:driverList){
			if(driver.getDriverId()<minId && driver.getCab().getAvailable()==true){
				minId=driver.getDriverId();
				bookdriver=driver;
			}
		}

		if(bookdriver==null){
			throw new Exception("No value present");
		}

		Customer customer= customerRepository2.findById(customerId).get();

		TripBooking tripBooking=new TripBooking(fromLocation,toLocation,distanceInKm);
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setDistanceInKm(distanceInKm);
	    tripBooking.setCustomer(customer);
		tripBooking.setStatus(TripStatus.CONFIRMED);
		tripBooking.setBill(distanceInKm*bookdriver.getCab().getPerKmRate());
		tripBooking.setDriver(bookdriver);

          //setting tripBooking for driver
		List<TripBooking> tripBookingList=bookdriver.getTripBookingList();

		if (tripBookingList==null){
			tripBookingList=new ArrayList<>();
		}
        bookdriver.setTripBookingList(tripBookingList);
		//setting trip booking for customer

		List<TripBooking> tripBookingListCustomer=customer.getTripBookingList();

		if (tripBookingListCustomer==null){
			tripBookingListCustomer=new ArrayList<>();
		}
		customer.setTripBookingList(tripBookingListCustomer);

		customerRepository2.save(customer);
		tripBookingRepository2.save(tripBooking);
		driverRepository2.save(bookdriver);

		return tripBooking;
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly

		TripBooking tripBooking=tripBookingRepository2.findById(tripId).get();
		tripBooking.getDriver().getCab().setAvailable(true);
		tripBooking.setStatus(TripStatus.CANCELED);
		tripBooking.setBill(0);

		tripBookingRepository2.save(tripBooking);


	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking= tripBookingRepository2.findById(tripId).get();
		tripBooking.getDriver().getCab().setAvailable(true);
		tripBooking.setStatus(TripStatus.COMPLETED);

		tripBookingRepository2.save(tripBooking);
	}
}
