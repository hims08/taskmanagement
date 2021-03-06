package com.tutor.taskmanagement.task.controller;

import com.tutor.taskmanagement.task.TaskMapper;
import com.tutor.taskmanagement.task.dao.TaskDAO;
import com.tutor.taskmanagement.task.dto.TaskDTO;
import com.tutor.taskmanagement.task.entities.Task;
import com.tutor.taskmanagement.task.pagination.PagerModel;
import com.tutor.taskmanagement.user.enitites.User;
import com.tutor.taskmanagement.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.text.ParseException;
import java.util.Optional;

@Controller
public class TaskController {
    private static final int BUTTONS_TO_SHOW = 5;
    private static final int[] PAGE_SIZES = {5, 10, 25, 50, 100};
    @Autowired
    private TaskDAO taskDAO;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private UserRepository userRepo;


    @GetMapping("/home")
    @PreAuthorize("hasAnyAuthority('ADMIN','USER')")
    public ModelAndView getIndex(Optional<Integer> page, Optional<Integer> pageSize, Optional<String> taskName, HttpServletRequest request) {
        /*Get user details*/
        String userEmail = request.getUserPrincipal().getName();
        User user = userRepo.findByEmail(userEmail);
        ModelAndView mv = new ModelAndView("index");

        int evalPageSize = pageSize.orElse(10);
        int evalPage = (page.orElse(0) < 1) ? 0 : page.get() - 1; //ternary operator

        Pageable pageable = PageRequest.of(evalPage, evalPageSize);
        /*Find all tasks*/
        Page<Task> tasks = taskDAO.findAllByUserId(pageable, user.getId(), taskName.orElse("_"));
        PagerModel pager = new PagerModel(tasks.getTotalPages(), tasks.getNumber(), BUTTONS_TO_SHOW);
        mv.addObject("selectedPageSize", evalPageSize);
        mv.addObject("pageSizes", PAGE_SIZES);
        mv.addObject("pager", pager);
        mv.addObject("tasks", tasks);

        /*Get Username*/
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        mv.addObject("username", user.getFirstName() + ' ' + user.getLastName());
        return mv;
    }

    @GetMapping("/task/add")
    public ModelAndView showAddTaskForm() {
        ModelAndView mv = new ModelAndView("add-task-form");
        mv.addObject("taskDTO", new TaskDTO());
        return mv;
    }

    @PostMapping("/task/post")
    public String createNewPost(@Valid TaskDTO taskDTO, BindingResult result, HttpServletRequest request) throws ParseException {
        if (result.hasErrors()) {
            return "add-task-form";
        }
        if (taskDTO.getId() == null) {
            /*Get user details*/
            String userEmail = request.getUserPrincipal().getName();
            User user = userRepo.findByEmail(userEmail);
            //Create
            Task task = taskMapper.convertToEntity(taskDTO);
            task.setUserId(user.getId());
            task.setStatus(taskDTO.getStatus());

            Task savedTask = taskDAO.save(task);
            System.out.println("SAVED TASK ID IS: " + savedTask.getId());
        } else {
            //update
            Task task = taskDAO.findById(taskDTO.getId());
            task.setTaskName(taskDTO.getTaskName());
            task.setIssueDate(taskDTO.getIssueDateConverted());
            task.setCompletionDate(taskDTO.getCompletionDateConverted());
            task.setStatus(taskDTO.getStatus());
            Task updatedTask = taskDAO.save(task);
            System.out.println("UPDATED TASK ID IS: " + updatedTask.getId());
        }
        return "redirect:/home";
    }

    @GetMapping("/task/update/{id}")
    public ModelAndView updateTask(@PathVariable Long id) {
        ModelAndView mv = new ModelAndView("add-task-form");

        /*Convert entity to DTO*/
        Task task = taskDAO.findById(id);
        TaskDTO taskDTO = taskMapper.convertToDTO(task);
        mv.addObject("taskDTO", taskDTO);

        return mv;
    }

    @DeleteMapping("/task/delete/{id}")
    @ResponseBody
    public String deleteTask(@PathVariable Long id) {
        taskDAO.deleteTask(id);
        return "OK";
    }
}
