import java.io.*;
import java.util.*;

public class TrainBookingAI {

    static Scanner sc = new Scanner(System.in);
    static int acBookings = 0;
    static int sleeperBookings = 0;

    public static void main(String[] args) throws Exception {

        // Load train data from file
        Map<String, List<Coach>> trains = loadTrains();

        System.out.println("🚆 AVAILABLE TRAINS & SEATS");
        displayAvailability(trains);

        System.out.print("\nEnter Train Number: ");
        String trainNo = sc.nextLine();

        System.out.print("Enter Passenger Age: ");
        int age = sc.nextInt();

        // AI – Coach decision
        String coachType = decideCoach(age);
        System.out.println("AI Coach Allocation: " + coachType);

        Coach coach = getCoach(trains, trainNo, coachType);

        if (coach == null || coach.totalSeats == 0) {
            System.out.println("Preferred coach full. Switching coach...");
            coachType = coachType.equals("AC") ? "SLEEPER" : "AC";
            coach = getCoach(trains, trainNo, coachType);
        }

        if (coach == null || coach.totalSeats == 0) {
            System.out.println("❌ No seats available.");
            return;
        }

        // AI – Seat Recommendation
        String recommendation = recommendSeat(age);
        System.out.println("AI Seat Recommendation: " + recommendation);

        System.out.println("\nAvailable Seats:");
        System.out.println("Aisle: " + coach.aisleSeats);
        System.out.println("Window: " + coach.windowSeats);

        System.out.print("Choose seat type (AISLE/WINDOW): ");
        sc.nextLine(); // consume leftover newline
        String seatType = sc.nextLine().toUpperCase();

        if (!coach.bookSeat(seatType)) {
            System.out.println("❌ Selected seat type unavailable.");
            return;
        }

        // AI – Priority Score
        int priority = priorityScore(age);

        saveBooking(trainNo, coachType, seatType, age, priority);
        updateTrainFile(trains);

        // Demand Prediction
        predictDemand();

        System.out.println("\n✅ Booking Successful!");
        System.out.println("Priority Score: " + priority);
    }

    // ================= AI METHODS =================

    static String decideCoach(int age) {
        return (age >= 60 || age <= 12) ? "AC" : "SLEEPER";
    }

    static String recommendSeat(int age) {
        if (age >= 60) return "Window seat (easy movement)";
        if (age <= 12) return "Window seat (comfort)";
        return "Aisle seat (mobility)";
    }

    static int priorityScore(int age) {
        if (age >= 60) return 90;
        if (age <= 12) return 70;
        return 50;
    }

    static void predictDemand() {
        if (acBookings > sleeperBookings)
            System.out.println("📊 AI Prediction: AC coach is in higher demand");
        else if (sleeperBookings > acBookings)
            System.out.println("📊 AI Prediction: Sleeper coach is in higher demand");
        else
            System.out.println("📊 AI Prediction: Equal demand for both coaches");
    }

    // ================= FILE HANDLING =================

    static Map<String, List<Coach>> loadTrains() throws Exception {
        Map<String, List<Coach>> map = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader("train.txt"));
        String line;

        while ((line = br.readLine()) != null) {
            String[] p = line.split(",");
            Coach c = new Coach(p[1], Integer.parseInt(p[2]),
                    Integer.parseInt(p[3]), Integer.parseInt(p[4]));
            map.computeIfAbsent(p[0], k -> new ArrayList<>()).add(c);
        }
        br.close();
        return map;
    }

    static void updateTrainFile(Map<String, List<Coach>> trains) throws Exception {
        BufferedWriter bw = new BufferedWriter(new FileWriter("train.txt"));

        for (String t : trains.keySet()) {
            for (Coach c : trains.get(t)) {
                bw.write(t + "," + c.type + "," + c.totalSeats + "," +
                        c.aisleSeats + "," + c.windowSeats);
                bw.newLine();
            }
        }
        bw.close();
    }

    static void saveBooking(String train, String coach, String seat,
                            int age, int priority) throws Exception {
        BufferedWriter bw = new BufferedWriter(new FileWriter("bookings.txt", true));
        bw.write(train + "," + coach + "," + seat + "," + age + "," + priority);
        bw.newLine();
        bw.close();

        if (coach.equals("AC")) acBookings++;
        else sleeperBookings++;
    }

    static Coach getCoach(Map<String, List<Coach>> trains, String train, String type) {
        if (!trains.containsKey(train)) return null;
        for (Coach c : trains.get(train))
            if (c.type.equals(type)) return c;
        return null;
    }

    static void displayAvailability(Map<String, List<Coach>> trains) {
        for (String t : trains.keySet()) {
            System.out.println("Train: " + t);
            for (Coach c : trains.get(t)) {
                System.out.println("  " + c.type +
                        " | Total: " + c.totalSeats +
                        " | Aisle: " + c.aisleSeats +
                        " | Window: " + c.windowSeats);
            }
        }
    }
}

// ================= COACH CLASS =================

class Coach {
    String type;
    int totalSeats;
    int aisleSeats;
    int windowSeats;

    Coach(String t, int total, int aisle, int window) {
        type = t;
        totalSeats = total;
        aisleSeats = aisle;
        windowSeats = window;
    }

    boolean bookSeat(String seatType) {
        if (seatType.equals("AISLE") && aisleSeats > 0) {
            aisleSeats--;
        } else if (seatType.equals("WINDOW") && windowSeats > 0) {
            windowSeats--;
        } else {
            return false;
        }
        totalSeats--;
        return true;
    }
}