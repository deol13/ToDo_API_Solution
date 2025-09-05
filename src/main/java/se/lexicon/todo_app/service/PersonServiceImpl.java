package se.lexicon.todo_app.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.lexicon.notify.model.Email;
import se.lexicon.notify.service.MessageService;
import se.lexicon.todo_app.dto.PersonDto;
import se.lexicon.todo_app.dto.PersonRegistrationDto;
import se.lexicon.todo_app.entity.Person;
import se.lexicon.todo_app.entity.Role;
import se.lexicon.todo_app.entity.User;
import se.lexicon.todo_app.repository.PersonRepository;
import se.lexicon.todo_app.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PersonServiceImpl implements PersonService {
    private final PersonRepository personRepository;
    private final MessageService<Email> emailService;

    // To use notify-util-spring module for email service inject its dependency (MessageService<Email> emailService)

    private final UserRepository userRepository;

    private PasswordEncoder passwordEncoder;

    public PersonServiceImpl(PersonRepository personRepository, UserRepository userRepository, PasswordEncoder passwordEncoder, MessageService<Email> emailService) {
        this.personRepository = personRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }


    @Override
    public List<PersonDto> findAll() {
        return personRepository.findAll().stream()
                .map(person -> new PersonDto(person.getId(), person.getName(), person.getEmail()))
                .collect(Collectors.toList());
    }

    @Override
    public PersonDto findById(Long id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found"));
        return new PersonDto(person.getId(), person.getName(), person.getEmail());
    }


    @Override
    public PersonDto create(PersonRegistrationDto dto) {
        // Validate unique constraints
        if (personRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (userRepository.existsByUsername(dto.username())) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Create User first
        User user = new User();
        user.setUsername(dto.username());
        user.setPassword(passwordEncoder.encode(dto.password())); // Use the actual password from DTO
        user.addRole(Role.USER);

        // Create Person and establish bidirectional relationship
        Person person = new Person(dto.name(), dto.email());
        person.setUser(user);
        user.setPerson(person);

        // Save Person (cascade will save User)
        person = personRepository.save(person);

         emailService.sendMessage(new Email(person.getEmail(), "Welcome to Todo App", "Hello " + person.getName() + ",\n\nThank you for registering with us!"));

         emailService.sendMessage(new Email(person.getEmail(), "Welcome to Todo App", "Hello " + person.getName() + ",\n\nThank you for registering with us!"));

        // Return DTO
        return convertToDto(person);
    }

    @Override
    public PersonDto update(Long id, PersonDto personDto) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found"));
        person.setName(personDto.name());
        person.setEmail(personDto.email());
        Person updatedPerson = personRepository.save(person);
        return new PersonDto(updatedPerson.getId(), updatedPerson.getName(), updatedPerson.getEmail());
    }

    @Override
    public void delete(Long id) {
        personRepository.deleteById(id);
    }

    @Override
    public PersonDto findByEmail(String email) {
        Person person = personRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Person not found"));
        return new PersonDto(person.getId(), person.getName(), person.getEmail());
    }

    @Override
    public void updatePassword(Long id, String newPassword) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found"));
        User user = person.getUser();
        if (user != null) {
            userRepository.updatePasswordByUsername(user.getUsername(), newPassword);
        }
    }

    @Override
    public void toggleExpired(Long id, boolean status) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found"));
        User user = person.getUser();
        if (user != null) {
            userRepository.updateExpiredByUsername(user.getUsername(), status);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return personRepository.existsByEmail(email);
    }

    @Override
    public void addRole(Long id, Role role) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found"));
        User user = person.getUser();
        if (user != null) {
            user.addRole(role);
        }
    }

    @Override
    public void removeRole(Long id, Role role) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found"));
        User user = person.getUser();
        if (user != null) {
            user.removeRole(role);
        }
    }

    private PersonDto convertToDto(Person person) {
        return PersonDto.builder()
                .id(person.getId())
                .name(person.getName())
                .email(person.getEmail())
                .build();
    }


}
