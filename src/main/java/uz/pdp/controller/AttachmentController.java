package uz.pdp.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import uz.pdp.entity.Attachment;
import uz.pdp.entity.AttachmentContent;
import uz.pdp.repository.AttachmentRepository;
import uz.pdp.repository.AttachmentContentRepository;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/attachment")
public class AttachmentController {

    @Autowired
    AttachmentRepository attachmentRepository;

    @Autowired
    AttachmentContentRepository attachmentContentRepository;

    @PostMapping("/upload")
    public String uploadFileToDb(MultipartHttpServletRequest request) throws IOException {

        Iterator<String> fileNames = request.getFileNames();
        MultipartFile file = request.getFile(fileNames.next());
        if (file != null) {
             Attachment attachment = new Attachment();
            attachment.setOriginalName(file.getOriginalFilename());
            attachment.setSize(file.getSize());
            attachment.setContentType(file.getContentType());

            Attachment savedAttachment = attachmentRepository.save(attachment);

             AttachmentContent content = new AttachmentContent();
            content.setInternContent(file.getBytes());
            content.setAttachment(savedAttachment);
            attachmentContentRepository.save(content);
            return "file saved  id: " + savedAttachment.getId();
        }
        return "file not saved. Error";
    }


    @GetMapping("/info")
    public List<Attachment> infoFiles()  {
        return attachmentRepository.findAll();
    }

    @GetMapping("/info/{id}")
    public Attachment infoByFileId(@PathVariable Integer id) {
        Optional<Attachment> optionalAttachment = attachmentRepository.findById(id);
        if (optionalAttachment.isPresent()) {
            Attachment attachment = optionalAttachment.get();
            return attachment;
        }
        return new Attachment();
    }


    @GetMapping("/download/{id}")
    public void downloadFileById(@PathVariable Integer id, HttpServletResponse response) throws IOException {

        Optional<Attachment> optionalAttachment = attachmentRepository.findById(id);
        if (optionalAttachment.isPresent()) {
            Attachment attachment = optionalAttachment.get();

        Optional<AttachmentContent> contentOptional = attachmentContentRepository.findByAttachmentId(id);
        if (contentOptional.isPresent()) {
            AttachmentContent attachmentContent = contentOptional.get();

            response.setHeader("Content-Disposition", "attachment; filename=\"" + attachment.getOriginalName() + "\"");
            response.setContentType(attachment.getContentType());
            // FileCopyUtils.copy  convert byte to stream
            FileCopyUtils.copy(attachmentContent.getInternContent(), response.getOutputStream());
        }
     }
 }
}




