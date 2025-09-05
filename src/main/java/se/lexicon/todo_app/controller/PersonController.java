package se.lexicon.todo_app.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import se.lexicon.todo_app.dto.PersonDto;
import se.lexicon.todo_app.dto.PersonRegistrationDto;
import se.lexicon.todo_app.entity.Person;
import se.lexicon.todo_app.repository.PersonRepository;
import se.lexicon.todo_app.service.PersonService;

import java.util.List;

@RestController
@RequestMapping("/api/person")
@Validated
@SecurityRequirement(name = "Bearer Authentication")  // Add this annotation
@Tag(name = "Person API", description = "API endpoints for managing persons")
public class PersonController {

    PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK) // 200 OK
    public List<PersonDto> getPerson() {
        System.out.println("Fetching all persons");
        return personService.findAll();
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK) // 200 OK
    public PersonDto getPersonById(
            @PathVariable("id")
            @Positive(message = "Id must be a positive number")
            Long id) {
        System.out.println("id = " + id);
        return personService.findById(id);
    }



    @RolesAllowed({"ADMIN", "MODERATOR"})
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED) // 201 Created
    public PersonDto createPerson(@RequestBody @NotNull(message = "Person cannot be null") @Valid PersonRegistrationDto personDto) {
        System.out.println("Creating person: " + personDto);
        return personService.create(personDto);
    }

    // Allow only ADMIN, and check if user is trying to modify their own data
    @PreAuthorize("hasRole('ADMIN') or #personDto.userId == authentication.principal.id")
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204 No Content
    public void updatePerson(@PathVariable @NotNull(message = "Id cannot be null") Long id,
                             @RequestBody @NotNull(message = "Person cannot be null") PersonDto personDto) {
        System.out.println("Updating person with ID: " + id);
        personService.update(id, personDto);
    }

    // Using @Secured (alternative way)
    @Secured({"ROLE_ADMIN", "ROLE_MODERATOR"})
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204 No Content
    public void deletePerson(@PathVariable @NotNull(message = "Id cannot be null") Long id) {
        System.out.println("Deleting person with ID: " + id);
        personService.delete(id);
    }

}
