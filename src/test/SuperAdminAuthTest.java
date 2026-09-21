package test;

import service.UserService;
import models.entity.UserEntity;
import models.Session;
import util.SecurityUtil;

import java.util.Optional;

public class SuperAdminAuthTest {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("RUNNING SUPER ADMIN AUTHENTICATION TESTS");
        System.out.println("==================================================");

        UserService userService = new UserService();
        int passed = 0;
        int failed = 0;

        // Reset any lockouts prior to test
        SecurityUtil.recordSuccessfulLogin("Bilawal");
        SecurityUtil.recordSuccessfulLogin("bilawal");

        // Test 1: Bilawal / breakthewall -> SUCCESS (SUPER_ADMIN)
        System.out.print("[Test 1] Username: Bilawal, Password: breakthewall -> ");
        Optional<UserEntity> test1 = userService.authenticate("Bilawal", "breakthewall");
        if (test1.isPresent() && "SUPER_ADMIN".equals(test1.get().getRole())) {
            System.out.println("PASSED (Role = SUPER_ADMIN)");
            passed++;
        } else {
            System.out.println("FAILED");
            failed++;
        }

        // Test 2: Bilawal / wrong -> FAIL
        System.out.print("[Test 2] Username: Bilawal, Password: wrong -> ");
        Optional<UserEntity> test2 = userService.authenticate("Bilawal", "wrong");
        if (!test2.isPresent()) {
            System.out.println("PASSED (Rejected - Invalid password)");
            passed++;
        } else {
            System.out.println("FAILED (Should have been rejected)");
            failed++;
        }

        // Test 3: Bilawal / "" -> FAIL
        System.out.print("[Test 3] Username: Bilawal, Password: (empty) -> ");
        Optional<UserEntity> test3 = userService.authenticate("Bilawal", "");
        if (!test3.isPresent()) {
            System.out.println("PASSED (Rejected - Empty password)");
            passed++;
        } else {
            System.out.println("FAILED (Should have been rejected)");
            failed++;
        }

        // Test 4: bilawal (case-insensitive username) / breakthewall -> SUCCESS (SUPER_ADMIN)
        SecurityUtil.recordSuccessfulLogin("bilawal");
        System.out.print("[Test 4] Username: bilawal, Password: breakthewall -> ");
        Optional<UserEntity> test4 = userService.authenticate("bilawal", "breakthewall");
        if (test4.isPresent() && "SUPER_ADMIN".equals(test4.get().getRole())) {
            System.out.println("PASSED (Role = SUPER_ADMIN)");
            passed++;
        } else {
            System.out.println("FAILED");
            failed++;
        }

        // Test 5: Bilawal / BREAKTHEWALL (uppercase password) -> FAIL
        System.out.print("[Test 5] Username: Bilawal, Password: BREAKTHEWALL -> ");
        Optional<UserEntity> test5 = userService.authenticate("Bilawal", "BREAKTHEWALL");
        if (!test5.isPresent()) {
            System.out.println("PASSED (Rejected - Case-sensitive password required)");
            passed++;
        } else {
            System.out.println("FAILED (Should have been rejected)");
            failed++;
        }

        // Reset lockout from Test 5 failure
        SecurityUtil.recordSuccessfulLogin("Bilawal");

        // Test 6: Account Lockout Integration (5 consecutive failed attempts lock account)
        System.out.print("[Test 6] Account Lockout (5 failed attempts for Bilawal) -> ");
        for (int i = 0; i < 5; i++) {
            userService.authenticate("Bilawal", "badpass" + i);
        }
        boolean isLocked = SecurityUtil.isAccountLocked("Bilawal");
        Optional<UserEntity> lockedAttempt = userService.authenticate("Bilawal", "breakthewall");
        if (isLocked && !lockedAttempt.isPresent()) {
            System.out.println("PASSED (Account locked after 5 failed attempts)");
            passed++;
        } else {
            System.out.println("FAILED");
            failed++;
        }

        // Unlock account for Session test
        SecurityUtil.recordSuccessfulLogin("Bilawal");

        // Test 7: Session & Logout Integration
        System.out.print("[Test 7] Session login, permissions, and logout verification -> ");
        Optional<UserEntity> test7 = userService.authenticate("Bilawal", "breakthewall");
        if (test7.isPresent()) {
            Session.login(test7.get());
            boolean loggedInCheck = Session.isLoggedIn();
            boolean isSuperAdminCheck = Session.isSuperAdmin();
            boolean permissionCheck = Session.hasPermission("MANAGE_USERS") && Session.hasPermission("ANY_ACTION");
            
            // Perform Logout
            Session.logout();
            boolean loggedOutCheck = !Session.isLoggedIn() && Session.getUsername() == null && !Session.hasPermission("ANY_ACTION");

            if (loggedInCheck && isSuperAdminCheck && permissionCheck && loggedOutCheck) {
                System.out.println("PASSED (Session authenticated, SUPER_ADMIN permissions verified, session cleared on logout)");
                passed++;
            } else {
                System.out.println("FAILED");
                failed++;
            }
        }

        System.out.println("==================================================");
        System.out.println(String.format("TEST RESULTS: %d PASSED, %d FAILED", passed, failed));
        System.out.println("==================================================");
    }
}
