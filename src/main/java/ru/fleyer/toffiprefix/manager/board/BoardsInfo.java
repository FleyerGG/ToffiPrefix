package ru.fleyer.toffiprefix.manager.board;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * @author Fleyer
 * <p> BoardsInfo creation on 30.03.2023 at 10:46
 */
@RequiredArgsConstructor
@Getter
@Setter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BoardsInfo {
    String rgName;
    String title;
    List<String> lines;
}
