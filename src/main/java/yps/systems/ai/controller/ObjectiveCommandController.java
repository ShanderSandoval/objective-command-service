package yps.systems.ai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;
import yps.systems.ai.model.Objective;
import yps.systems.ai.repository.IObjectiveRepository;

import java.util.Optional;

@RestController
@RequestMapping("/command/objectiveService")
public class ObjectiveCommandController {

    private final IObjectiveRepository objectiveRepository;
    private final KafkaTemplate<String, Objective> kafkaTemplate;

    @Value("${env.kafka.topicEvent}")
    private String kafkaTopicEvent;

    @Autowired
    public ObjectiveCommandController(IObjectiveRepository objectiveRepository, KafkaTemplate<String, Objective> kafkaTemplate) {
        this.objectiveRepository = objectiveRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping
    public ResponseEntity<String> createObjective(@RequestBody Objective objective) {
        Objective savedObjective = objectiveRepository.save(objective);
        Message<Objective> message = MessageBuilder
                .withPayload(savedObjective)
                .setHeader(KafkaHeaders.TOPIC, kafkaTopicEvent)
                .setHeader("eventType", "CREATE_OBJECTIVE")
                .setHeader("source", "objectiveService")
                .build();
        kafkaTemplate.send(message);
        return new ResponseEntity<>("Objective saved with ID: " + savedObjective.getElementId(), HttpStatus.CREATED);
    }

    @DeleteMapping("/{elementId}")
    public ResponseEntity<String> deleteObjective(@PathVariable String elementId) {
        Optional<Objective> optionalObjective = objectiveRepository.findById(elementId);
        if (optionalObjective.isPresent()) {
            objectiveRepository.deleteById(elementId);
            Message<String> message = MessageBuilder
                    .withPayload(elementId)
                    .setHeader(KafkaHeaders.TOPIC, kafkaTopicEvent)
                    .setHeader("eventType", "DELETE_OBJECTIVE")
                    .setHeader("source", "objectiveService")
                    .build();
            kafkaTemplate.send(message);
            return new ResponseEntity<>("Objective deleted successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Objective not founded", HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{elementId}")
    public ResponseEntity<String> updateObjective(@PathVariable String elementId, @RequestBody Objective objective) {
        Optional<Objective> optionalObjective = objectiveRepository.findById(elementId);
        if (optionalObjective.isPresent()) {
            objective.setElementId(optionalObjective.get().getElementId());
            objectiveRepository.save(objective);
            Message<Objective> message = MessageBuilder
                    .withPayload(objective)
                    .setHeader(KafkaHeaders.TOPIC, kafkaTopicEvent)
                    .setHeader("eventType", "UPDATE_OBJECTIVE")
                    .setHeader("source", "objectiveService")
                    .build();
            kafkaTemplate.send(message);
            return new ResponseEntity<>("Objective updated successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Objective not founded", HttpStatus.NOT_FOUND);
        }
    }

}
