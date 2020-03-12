package com.app4.project.timelapseserver.repository.mongo;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Optional;

public class MongoRepository<T> {

  protected final MongoTemplate mongoTemplate;
  protected final Class<T> clazz;
  private final String collectionName;

  public MongoRepository(MongoTemplate mongoTemplate, Class<T> clazz, String collectionName) {
    this.mongoTemplate = mongoTemplate;
    this.clazz = clazz;
    this.collectionName = collectionName;
  }


  public int count() {
    return (int) mongoTemplate.count(new Query(), collectionName);
  }

  public List<T> getAll() {
    return mongoTemplate.findAll(clazz, collectionName);
  }

  public void add(T object) {
    mongoTemplate.insert(object, collectionName);
  }

  public Optional<T> getById(int id) {
    return Optional.ofNullable(
      mongoTemplate.findOne(queryById(id), clazz, collectionName)
    );
  }

  public boolean remove(int id) {
    return mongoTemplate.remove(queryById(id), clazz, collectionName).getDeletedCount() > 0;
  }

  protected Query queryById(int id) {
    return Query.query(Criteria.where("id").is(id));
  }

}
