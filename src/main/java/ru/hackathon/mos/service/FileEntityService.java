package ru.hackathon.mos.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hackathon.mos.entity.Document;
import ru.hackathon.mos.entity.FileEntity;
import ru.hackathon.mos.exception.FileNotFoundException;
import ru.hackathon.mos.repository.FileEntityRepository;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FileEntityService {
    private final FileEntityRepository fileEntityRepository;

    public Long saveFile(Document document) {
        throw new NotImplementedException();
    }

    public FileEntity getFileEntityById(Long fileEntityId) {
        return fileEntityRepository.findById(fileEntityId)
                .orElseThrow(() -> new FileNotFoundException("Файл с ID " + fileEntityId + " не найден"));
    }
}