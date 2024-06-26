package com.mancity.social.highlight.application;

import com.mancity.social.game.domain.Game;
import com.mancity.social.game.domain.repository.GameRepository;
import com.mancity.social.game.exception.NoSuchGameException;
import com.mancity.social.highlight.application.dto.request.CreateHighlightRequestDto;
import com.mancity.social.highlight.application.dto.request.StoreHighlightRequestDto;
import com.mancity.social.highlight.application.dto.response.HighlightResponseDto;
import com.mancity.social.highlight.application.dto.response.MyhighlightResponseDto;
import com.mancity.social.highlight.domain.Highlight;
import com.mancity.social.highlight.domain.Myhighlight;
import com.mancity.social.highlight.domain.repository.HighlightRepository;
import com.mancity.social.highlight.exception.NoSuchHighlightException;
import com.mancity.social.user.application.dto.response.UserResponseDto;
import com.mancity.social.user.presentation.UserFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class HighlightService {

    private final GameRepository gameRepository;

    private final HighlightRepository highlightRepository;

    private final UserFeignClient userFeignClient;

    public void createHighlights(CreateHighlightRequestDto dto) {
        Game game = gameRepository.findById(dto.getGameId()).orElseThrow(NoSuchGameException::new);
//        highlightRepository.save(Highlight.builder()
//                .gameId(game.getId())
//                .myhighlights(new ArrayList<>())
//                .time(dto.getTime())
//                .build());
        game.updateHighlights(dto.toEntity(game.getReplayUrl(), game)); // game 에 저장 완료
    }

    public List<HighlightResponseDto> getGameHighlights(Long id) {
        Game game = gameRepository.findById(id).orElseThrow(NoSuchGameException::new);

        //gameId로 하이라이트 추출
        List<Highlight> highlights = highlightRepository.findByGameId(id);

        List<HighlightResponseDto> lists = new ArrayList<>();
        for (Highlight h : highlights) {
            lists.add(HighlightResponseDto.builder()
                    .id(h.getId())
                    .url(game.getReplayUrl())
                    .time(h.getTime())
                    .courtId(game.getCourtId())
                    .build());

        }
        return lists;
    }

    public void storeMyHighlight(StoreHighlightRequestDto dto) {
//        Long user = userFeignClient.findById(dto.getUserId()).getId();
        Highlight highlight = highlightRepository.findById(dto.getHighlightId()).orElseThrow(NoSuchHighlightException::new);

        //마이 하이라이트 생성 후 하이라이트와 유저에 업데이트
        Myhighlight myhighlight = Myhighlight.builder()
                .highlight(highlight)
                .userId(dto.getUserId())
                .build();

        //하이라이트의 마이하이라이트 리스트에 업데이트
        highlight.addStoredHighlights(myhighlight);


    }


    public List<MyhighlightResponseDto> getMyHighlights(Long id) {
//        UserResponseDto userDto = userFeignClient.findById(id);


        List<Myhighlight> myhighlights = highlightRepository.findAllByUserId(id);

        List<MyhighlightResponseDto> list = new ArrayList<>();
        for (Myhighlight h : myhighlights) {

//            Game game = gameRepository.findById(h.getHighlight().getGameId()).orElseThrow(NoSuchGameException::new);
            Game game = h.getHighlight().getGame();
            list.add(MyhighlightResponseDto.builder()
                    .id(h.getId())
                    .url(game.getReplayUrl())
                    .time(h.getHighlight().getTime())
                    .courtId(game.getCourtId())
                    .build());
        }

        return list;
    }


}
