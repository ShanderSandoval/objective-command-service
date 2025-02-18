package yps.systems.ai.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import yps.systems.ai.model.Objective;

@Repository
public interface IObjectiveRepository extends Neo4jRepository<Objective, String> {
}
