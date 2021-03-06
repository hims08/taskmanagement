package com.tutor.taskmanagement.task.dao.daoImpl;

import com.tutor.taskmanagement.task.dao.TaskDAO;
import com.tutor.taskmanagement.task.entities.Task;
import com.tutor.taskmanagement.task.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class TaskDAOImpl implements TaskDAO {
    @Autowired
    private TaskRepository taskRepository;

    @Override
    public Page<Task> findAll(Pageable pageable, String s) {
        return taskRepository.findAll(pageable, s);
    }

    @Override
    public Task findById(Long id) {
//        Task task = taskRepository.findById(id).orElseThrow(ClassNotFoundException::new);
        Task task = taskRepository.getOneById(id);
        return task;
    }

    @Override
    @Modifying
    @Transactional
    public Task save(Task task) {
        return taskRepository.save(task);
    }

    @Override
    @Modifying
    @Transactional
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    @Override
    public Page<Task> findAllByUserId(Pageable pageable,Long id, String s) {
        return taskRepository.findAllByUserId(pageable,id,s);
    }
}
