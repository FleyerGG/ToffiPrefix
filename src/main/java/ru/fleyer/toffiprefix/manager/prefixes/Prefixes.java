package ru.fleyer.toffiprefix.manager.prefixes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * @author Fleyer
 * <p> Prefixes creation on 22.03.2023 at 18:09
 */
@RequiredArgsConstructor
@Getter
@Setter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Prefixes {
    String prefix;
    int priority;
}
