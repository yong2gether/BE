package com.yong2gether.ywave.bookmark.service;

import com.yong2gether.ywave.bookmark.domain.Bookmark;
import com.yong2gether.ywave.bookmark.domain.BookmarkGroup;
import com.yong2gether.ywave.bookmark.repository.BookmarkGroupRepository;
import com.yong2gether.ywave.bookmark.repository.BookmarkRepository;
import com.yong2gether.ywave.store.domain.Store;
import com.yong2gether.ywave.store.repository.StoreRepository;
import com.yong2gether.ywave.user.domain.User;
import com.yong2gether.ywave.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final BookmarkGroupRepository bookmarkGroupRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    public Long create(Long userId, Long storeId, Long groupId) {
        if (bookmarkRepository.existsByUser_IdAndStore_Id(userId, storeId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 북마크된 가맹점입니다.");
        }

        User user = userRepository.getReferenceById(userId);
        Store store = storeRepository.getReferenceById(storeId);
        BookmarkGroup group = resolveGroup(user, groupId);

        Bookmark bookmark = Bookmark.of(user, store, group);
        bookmarkRepository.save(bookmark);
        return bookmark.getId();
    }

    private BookmarkGroup resolveGroup(User user, Long groupId) {
        if (groupId != null) {
            BookmarkGroup g = bookmarkGroupRepository.findById(groupId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "그룹을 찾을 수 없습니다."));
            if (!g.getUser().getId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "다른 사용자의 그룹입니다.");
            }
            return g;
        }
        return bookmarkGroupRepository.findByUserIdAndIsDefaultTrue(user.getId())
                .orElseGet(() -> bookmarkGroupRepository.save(
                        BookmarkGroup.builder().user(user).name("기본").isDefault(true).build()
                ));
    }
    public boolean isBookmarked(Long userId, Long storeId) {
        if(userId == null) return false;
        return bookmarkRepository.existsByUser_IdAndStore_Id(userId, storeId);
    }
}
