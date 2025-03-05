package com.shopfloor.backend;

import com.shopfloor.backend.database.objects.Role;
import com.shopfloor.backend.database.objects.RoleDBO;
import com.shopfloor.backend.database.objects.UserDBO;
import com.shopfloor.backend.database.repositories.RoleRepository;
import com.shopfloor.backend.database.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * This class initializes the database with test users and roles.
 * It ensures that three roles (OPERATOR and EDITOR) and their corresponding users are created.
 *
 * Note: If you add a new user and a new role for them, follow these steps:
 * 1. Create the users.
 * 2. Create the role.
 * 3. Associate the user with the role.
 * 4. Save the role in the database first.
 * 5. Save the user.
 *
 * These steps are important due to the JPA mappings used.
 * @author David Todorov (https://github.com/david-todorov)
 */
@Transactional
@Component
public class DBInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Constructor for DBInitializer.
     *
     * @param userRepository the repository for user data access
     * @param roleRepository the repository for role data access
     */
    @Autowired
    public DBInitializer(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Initializes the database with test users and roles.
     * This method is called after the bean's properties have been set.
     */
    @PostConstruct
    public void init() {

        if (!roleRepository.existsByName(Role.OPERATOR)) {
            RoleDBO operatorRole = new RoleDBO(Role.OPERATOR);
            roleRepository.save(operatorRole);
        }

        if (!roleRepository.existsByName(Role.EDITOR)) {
            RoleDBO editorRole = new RoleDBO(Role.EDITOR);
            roleRepository.save(editorRole);
        }

        if (!roleRepository.existsByName(Role.OLINGO)) {
            RoleDBO olingoRole = new RoleDBO(Role.OLINGO);
            roleRepository.save(olingoRole);
        }

        // Check if users already exist
        if (!userRepository.existsByUsername("operator")) {
            // Creating the operator user
            UserDBO operator = new UserDBO("operator", passwordEncoder.encode("operator"));
            operator.getRoles().add(roleRepository.findByName(Role.OPERATOR)); // Assign role
            userRepository.save(operator);
        }

        if (!userRepository.existsByUsername("editor")) {
            // Creating the editor user
            UserDBO editor = new UserDBO("editor", passwordEncoder.encode("editor"));
            editor.getRoles().add(roleRepository.findByName(Role.EDITOR)); // Assign role
            editor.getRoles().add(roleRepository.findByName(Role.OPERATOR)); // Also assign operator role
            userRepository.save(editor);
        }

        //Currently, the Olingo user is not used in the application.
        if (!userRepository.existsByUsername("olingo")) {
            UserDBO olingo = new UserDBO("olingo", passwordEncoder.encode("olingo"));
            olingo.getRoles().add(roleRepository.findByName(Role.OLINGO));
            userRepository.save(olingo);
        }
    }
}
