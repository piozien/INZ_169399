package pl.su.su_backend.service.classServ;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.model.classes.Classes;

import pl.su.su_backend.dto.user.UserResponseDto;
import pl.su.su_backend.dto.classes.ClassesRequestDto;
import pl.su.su_backend.dto.classes.ClassesResponseDto;
import pl.su.su_backend.dto.classes.ClassesMapper;
import pl.su.su_backend.dto.user.UserMapper;
import pl.su.su_backend.repositories.classRep.ClassesRepository;
import pl.su.su_backend.repositories.user.UsersRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClassesService {

    private final ClassesRepository classesRepository;
    private final UsersRepository usersRepository;

    public ClassesResponseDto create(ClassesRequestDto dto) {
        classesRepository.findByName(dto.getName()).ifPresent(c -> {
            throw new RuntimeException("Class already exists: " + dto.getName());
        });
        Classes c = ClassesMapper.toEntity(dto);
        return ClassesMapper.toResponse(classesRepository.save(c));
    }

    @Transactional(readOnly = true)
    public List<ClassesResponseDto> list() {
        return classesRepository.findAll().stream().map(ClassesMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClassesResponseDto get(UUID id) {
        var c = classesRepository.findById(id).orElseThrow(() -> new RuntimeException("Class not found: " + id));
        return ClassesMapper.toResponse(c);
    }

    public ClassesResponseDto update(UUID id, ClassesRequestDto dto) {
        Classes c = classesRepository.findById(id).orElseThrow(() -> new RuntimeException("Class not found: " + id));
        c.setName(dto.getName());
        c.setYear(dto.getYear());
        return ClassesMapper.toResponse(classesRepository.save(c));
    }

    public void delete(UUID id) {
        classesRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getUsers(UUID classId) {
        return usersRepository.findByClasses_Id(classId).stream()
                .map(UserMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    public void detachUser(UUID userId) {
        var user = usersRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setClasses(null);
        usersRepository.save(user);
    }

}


