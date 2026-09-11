package com.synccarreira.synccarreira_api.services;

import com.synccarreira.synccarreira_api.dto.AnswerDTO;
import com.synccarreira.synccarreira_api.dto.TrailDTO;
import com.synccarreira.synccarreira_api.dto.TrailUpdateDTO;
import com.synccarreira.synccarreira_api.dto.UserDTO;
import com.synccarreira.synccarreira_api.entities.Answer;
import com.synccarreira.synccarreira_api.entities.Question;
import com.synccarreira.synccarreira_api.entities.Trail;
import com.synccarreira.synccarreira_api.entities.User;
import com.synccarreira.synccarreira_api.repositories.AnswerRepository;
import com.synccarreira.synccarreira_api.repositories.QuestionRepository;
import com.synccarreira.synccarreira_api.repositories.TrailRepository;
import com.synccarreira.synccarreira_api.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TrailService {

    @Autowired
    private TrailRepository trailRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Transactional(readOnly = true)
    public List<TrailDTO> findAll() {
        return trailRepository.findAll()
                .stream()
                .map(TrailDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public TrailDTO findById(Long id) {
        Trail trail = trailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trilha não encontrada. ID: " + id));
        return new TrailDTO(trail);
    }

    @Transactional
    public TrailDTO create(TrailDTO dto) {
        Trail trail = new Trail();
        trail.setName(dto.name());
        trail.setSequentialOrder(dto.sequentialOrder());
        trail = trailRepository.save(trail);
        return new TrailDTO(trail);
    }

    @Transactional
    public TrailDTO update(Long id, TrailUpdateDTO dto) {
        Trail trail = trailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trilha não encontrada. ID: " + id));
        Trail trail1 = trailRepository.findBySequentialOrder(dto.sequentialOrder()).orElse(null);
        if (trail1 != null) {
            List<Trail> trails = new ArrayList<>();
            trail1.setSequentialOrder(trail.getSequentialOrder());
            trail.setSequentialOrder(dto.sequentialOrder());
            trails.add(trail);
            trails.add(trail1);
            trailRepository.saveAll(trails);
        } else {
            trail.setName(dto.name());
            trail.setSequentialOrder(dto.sequentialOrder());
            trail = trailRepository.save(trail);
        }
        return new TrailDTO(trail);
    }

    @Transactional
    public void delete(Long id) {
        if (!trailRepository.existsById(id)) {
            throw new ResourceNotFoundException("Trilha não encontrada. ID: " + id);
        }
        trailRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean canAccess(Long trailId) {
        Trail trail = trailRepository.findById(trailId)
                .orElseThrow(() -> new ResourceNotFoundException("Trilha não encontrada. ID: " + trailId));

        if (trail.getSequentialOrder() == 1) {
            return true;
        }

        int previousOrder = trail.getSequentialOrder() - 1;
        Trail previousTrail = trailRepository.findBySequentialOrder(previousOrder)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trilha anterior (ordem " + previousOrder + ") não encontrada."));

        UserDTO userDto = userService.findMe();
        List<Answer> answers = answerRepository.findByStudentAndTrail(userDto.getId(), previousTrail.getId());
        List<Long> answeredIds = new ArrayList<>();
        answers.forEach(answer -> answeredIds.add(answer.getQuestionOption().getQuestion().getId()));

        return previousTrail.isConcluded(answeredIds);
    }
}
