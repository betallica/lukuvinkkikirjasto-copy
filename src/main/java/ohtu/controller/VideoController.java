package ohtu.controller;

import ohtu.database.dto.CommentDto;
import ohtu.database.dto.VideoHintDto;
import ohtu.model.Hint;
import ohtu.model.VideoHint;
import ohtu.service.CommentService;
import ohtu.service.HintService;
import ohtu.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import javax.validation.Valid;
import ohtu.database.dto.BlogHintDto;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class VideoController {

    @Autowired
    private HintService hintService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private TagService tagService;

    @GetMapping("/video/add")
    public String addVideo(Model model) {
        VideoHintDto vhDto = new VideoHintDto();

        model.addAttribute("videoHintDto", vhDto);
        model.addAttribute("allTags", tagService.getAllTags());

        return "add_video";
    }
    
    @GetMapping("/videos/{id}/edit")
    public String editVideo(Model model, @PathVariable long id) throws Exception {
        VideoHintDto videoHintDto = (VideoHintDto) hintService.getHintDto(id);
        model.addAttribute("videoHintDto", videoHintDto);
        model.addAttribute("videoHintId", id);
        model.addAttribute("allTags", tagService.getAllTags());
        return "edit_video";
    }
    
    @PostMapping("/videos/{id}/edit")
    public String saveVideoEdit(Model model, @ModelAttribute @Valid VideoHintDto videoHintDto, BindingResult result, @PathVariable long id) {
        if (!result.hasErrors()) {
            hintService.editHint(id, videoHintDto);

            return "redirect:/videos/{id}";
        } else {
            
            model.addAttribute("videoHint", hintService.getHint(id));
            model.addAttribute("videoHintDto", videoHintDto);
            model.addAttribute("videoHintId", id);
            model.addAttribute("allTags", tagService.getAllTags());
            return "edit_video";
        }
    }

    @PostMapping("/video/add")
    public String saveVideo(Model model, @ModelAttribute @Valid VideoHintDto videoHintDto, BindingResult result, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            hintService.createHint(videoHintDto);
            redirect.addFlashAttribute("notification", "Videovinkki lisätty!");
            return "redirect:/";
        } else {
            model.addAttribute("videoHintDto", videoHintDto);
            model.addAttribute("allTags", tagService.getAllTags());
            return "add_video";
        }
    }

    @GetMapping("/videos/{id}")
    public String getVideo(Model model, @PathVariable long id) {
        model.addAttribute("videoHint", hintService.getHint(id));
        model.addAttribute("comments", commentService.getCommentsForHint(id));

        CommentDto commentDto = new CommentDto();
        model.addAttribute("commentDto", commentDto);
        return "video";
    }

    @PostMapping(value = "/videos/{id}", params = "text")
    public String addCommentForVideo(Model model, @ModelAttribute @Valid CommentDto commentDto, BindingResult result,
                                     @PathVariable long id, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            commentDto.setHint(hintService.getHint(id));
            commentService.createComment(commentDto);
        } else {
            model.addAttribute("videoHint", hintService.getHint(id));
            model.addAttribute("comments", commentService.getCommentsForHint(id));

            model.addAttribute("commentDto", commentDto);

            return "video";
        }
        redirect.addFlashAttribute("notification", "Kommentti lisätty!");
        return "redirect:/videos/" + id;
    }

    @PostMapping(value = "/videos/{id}", params = "isRead")
    public String markVideoAsRead(Model model, @PathVariable long id) {
        Hint hint = hintService.getHint(id);
        if (hint.getIsRead()) {
            hint.setIsRead(false);
        } else {
            hint.setIsRead(true);
        }
        hintService.saveHint(hint);

        return "redirect:/videos/" + id;
    }

}
