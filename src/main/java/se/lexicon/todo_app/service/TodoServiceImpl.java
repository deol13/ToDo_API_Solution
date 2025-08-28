package se.lexicon.todo_app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.lexicon.todo_app.dto.TodoDto;
import se.lexicon.todo_app.entity.Person;
import se.lexicon.todo_app.entity.Todo;
import se.lexicon.todo_app.repository.PersonRepository;
import se.lexicon.todo_app.repository.TodoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;
    private final PersonRepository personRepository;

    public TodoServiceImpl(TodoRepository todoRepository, PersonRepository personRepository) {
        this.todoRepository = todoRepository;
        this.personRepository = personRepository;
    }

    private TodoDto convertToDto(Todo todo) {
        return TodoDto.builder()
                .id(todo.getId())
                .title(todo.getTitle())
                .description(todo.getDescription())
                .completed(todo.isCompleted())
                .createdAt(todo.getCreatedAt())
                .updatedAt(todo.getUpdatedAt())
                .dueDate(todo.getDueDate())
                .personId(todo.getPerson() != null ? todo.getPerson().getId() : null)
                .numberOfAttachments(todo.getAttachments().size())
                .build();
    }

    private Todo convertToEntity(TodoDto todoDto) {
        Todo todo = new Todo(
                todoDto.title(),
                todoDto.description(),
                todoDto.completed(),
                todoDto.dueDate()
        );
        
        if (todoDto.personId() != null) {
            Person person = personRepository.findById(todoDto.personId())
                    .orElseThrow(() -> new RuntimeException("Person not found"));
            todo.setPerson(person);
        }
        
        return todo;
    }

    @Override
    public TodoDto create(TodoDto todoDto) {
        Todo todo = convertToEntity(todoDto);
        Todo savedTodo = todoRepository.save(todo);
        return convertToDto(savedTodo);
    }

    @Override
    public TodoDto findById(Long id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found"));
        return convertToDto(todo);
    }

    @Override
    public List<TodoDto> findAll() {
        return todoRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public TodoDto update(Long id, TodoDto todoDto) {
        Todo existingTodo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found"));
        
        existingTodo.setTitle(todoDto.title());
        existingTodo.setDescription(todoDto.description());
        existingTodo.setCompleted(todoDto.completed());
        existingTodo.setDueDate(todoDto.dueDate());
        
        if (todoDto.personId() != null) {
            Person person = personRepository.findById(todoDto.personId())
                    .orElseThrow(() -> new RuntimeException("Person not found"));
            existingTodo.setPerson(person);
        } else {
            existingTodo.setPerson(null);
        }
        
        Todo updatedTodo = todoRepository.save(existingTodo);
        return convertToDto(updatedTodo);
    }

    @Override
    public void delete(Long id) {
        todoRepository.deleteById(id);
    }

    @Override
    public List<TodoDto> findByPersonId(Long personId) {
        return todoRepository.findByPerson_Id(personId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TodoDto> findByCompleted(boolean completed) {
        return todoRepository.findByCompleted(completed).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TodoDto> findOverdueTodos() {
        return todoRepository.findByDueDateBeforeAndCompletedFalse(LocalDateTime.now()).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}