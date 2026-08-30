package com.launchcode.moviemanager.controllers;

import com.launchcode.moviemanager.models.Movie;
import com.launchcode.moviemanager.models.data.MovieRepository;
import com.launchcode.moviemanager.services.AiMovieService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/movies")
public class MovieController {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private AiMovieService aiMovieService;

    @GetMapping({"", "/"})
    public String index(Model model) {
        model.addAttribute("movies", movieRepository.findAll());
        model.addAttribute("title", "My Movie Collection");
        return "movies/index";
    }

    @GetMapping("/add")
    public String displayAddMovieForm(Model model) {
        model.addAttribute("title", "Add Movie");
        model.addAttribute(new Movie());
        return "movies/add";
    }

    @PostMapping("/add")
    public String processAddMovieForm(@ModelAttribute @Valid Movie newMovie, Errors errors, Model model) {
        if (errors.hasErrors()) {
            model.addAttribute("title", "Add Movie");
            return "movies/add";
        }
        movieRepository.save(newMovie);
        return "redirect:/movies";
    }

    @GetMapping("/view/{movieId}")
    public String viewMovie(@PathVariable int movieId, Model model) {
        Optional<Movie> result = movieRepository.findById(movieId);
        if (result.isEmpty()) {
            return "redirect:/movies";
        }
        model.addAttribute("movie", result.get());
        model.addAttribute("title", result.get().getTitle());
        model.addAttribute("aiConfigured", aiMovieService.isConfigured());
        return "movies/view";
    }

    @GetMapping("/edit/{movieId}")
    public String displayEditForm(@PathVariable int movieId, Model model) {
        Optional<Movie> result = movieRepository.findById(movieId);
        if (result.isEmpty()) {
            return "redirect:/movies";
        }
        model.addAttribute("title", "Edit Movie");
        model.addAttribute("movie", result.get());
        return "movies/edit";
    }

    @PostMapping("/edit/{movieId}")
    public String processEditForm(@PathVariable int movieId, @ModelAttribute @Valid Movie updatedMovie,
                                  Errors errors, Model model) {
        if (errors.hasErrors()) {
            model.addAttribute("title", "Edit Movie");
            return "movies/edit";
        }
        updatedMovie.setId(movieId);
        movieRepository.save(updatedMovie);
        return "redirect:/movies/view/" + movieId;
    }

    @GetMapping("/delete/{movieId}")
    public String deleteMovie(@PathVariable int movieId) {
        movieRepository.deleteById(movieId);
        return "redirect:/movies";
    }

    @GetMapping("/{movieId}/ai-summary")
    public String generateAiSummary(@PathVariable int movieId) {
        Optional<Movie> result = movieRepository.findById(movieId);
        if (result.isPresent()) {
            Movie movie = result.get();
            String summary = aiMovieService.generateSummary(movie);
            movie.setAiSummary(summary);
            movieRepository.save(movie);
        }
        return "redirect:/movies/view/" + movieId;
    }

    @GetMapping("/{movieId}/ai-recommendations")
    public String generateAiRecommendations(@PathVariable int movieId) {
        Optional<Movie> result = movieRepository.findById(movieId);
        if (result.isPresent()) {
            Movie movie = result.get();
            List<Movie> collection = new ArrayList<>();
            movieRepository.findAll().forEach(collection::add);
            String recommendations = aiMovieService.generateRecommendations(movie, collection);
            movie.setAiRecommendations(recommendations);
            movieRepository.save(movie);
        }
        return "redirect:/movies/view/" + movieId;
    }
}