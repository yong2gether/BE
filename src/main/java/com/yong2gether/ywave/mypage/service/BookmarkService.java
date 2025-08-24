package com.yong2gether.ywave.mypage.service;

import com.yong2gether.ywave.bookmark.repository.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;

    public boolean isBookmarked(Long userId, Long storeId) {
        if(userId == null) return false;
        return bookmarkRepository.existsByUser_IdAndStore_Id(userId, storeId);
    }

}
