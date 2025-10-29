package com.mobile.server.domain.file.respository;

import com.mobile.server.domain.file.domain.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {
}
