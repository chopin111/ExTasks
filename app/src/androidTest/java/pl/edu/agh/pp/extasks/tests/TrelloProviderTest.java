package pl.edu.agh.pp.extasks.tests;

import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.trello4j.Trello;
import org.trello4j.model.Board;
import org.trello4j.model.Card;
import org.trello4j.model.Member;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import pl.edu.agh.pp.extasks.framework.Note;
import pl.edu.agh.pp.extasks.framework.TrelloProvider;

import static org.mockito.Mockito.when;

/**
 * Class testing TrelloProvider.class with Trello from Trello4J mocked.
 *
 * @author Jakub Lasisz
 * @author Maciej Sipko
 */
public class TrelloProviderTest extends TestCase {
    @Mock
    private Trello trelloManagerMock;
    private TrelloProvider trelloProvider;
    private String key;
    private String token;
    private Member member;
    private List<Board> boardList = new LinkedList<Board>();
    Map<String, List<Card>> cardsListByBoardId = new HashMap<String, List<Card>>();


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        key = "key";
        token = "token";
        trelloProvider = new TrelloProvider(key, token);
        injectTrelloMock();
        member = mockMember("1");
        addBoard("ID1", "Name1");
        addCard("IDCard1", "NameCard1", "TextCard1", "ID1");
        when(trelloManagerMock.getMemberByToken(token)).thenReturn(member);
        when(trelloManagerMock.getBoardsByMember(member.getId())).thenReturn(boardList);
        when(trelloManagerMock.getCardsByBoard("ID1")).thenReturn(cardsListByBoardId.get("ID1"));

    }

    private void injectTrelloMock() {
        try {
            Field field = trelloProvider.getClass().getDeclaredField("trelloManager");
            field.setAccessible(true);
            field.set(trelloProvider, trelloManagerMock);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private Member mockMember(String memberId) {
        Member member = new Member();
        member.setId(memberId);
        return member;
    }

    private void addBoard(String id, String name) {
        Board board = new Board();
        board.setId(id);
        board.setName(name);
        boardList.add(board);
    }

    private void addCard(String id, String name, String text, String boardId) {
        Card card = new Card();
        card.setId(id);
        card.setName(name);
        card.setDesc(text);
        if (!cardsListByBoardId.containsKey(boardId)) {
            cardsListByBoardId.put(boardId, new LinkedList<Card>());
        }
        cardsListByBoardId.get(boardId).add(card);
    }

    /**
     * Tests consistency and correctness of recieved Notes via TrelloProvider.
     */
    @SmallTest
    public void testGetNotesFromService() {
        //given
        addBoard("ID2", "Name2");
        addCard("IDCard2", "NameCard2", "TextCard2", "ID2");
        //when
        when(trelloManagerMock.getCardsByBoard("ID2")).thenReturn(cardsListByBoardId.get("ID2"));
        //then
        trelloProvider.getNotesFromService();
        List<Note> noteList = trelloProvider.getNotes();
        assertEquals(2, noteList.size());
        Note firstNote = noteList.get(0);
        assertEquals("NameCard1", firstNote.getTitle());
        assertEquals("TextCard1", firstNote.getText());
        Note secondNote = noteList.get(1);
        assertEquals("NameCard2", secondNote.getTitle());
        assertEquals("TextCard2", secondNote.getText());
    }

    /**
     * Ensures that closed boards are omitted during recieving Notes via TrelloProvider.
     */
    @SmallTest
    public void testGetNotesFromServiceWithClosedBoard() {
      // given
        Board board = new Board();
        board.setId("ID2");
        board.setName("Name2");
        board.setClosed(true);
        boardList.add(board);
        addCard("IDCard2", "NameCard2", "TextCard2", "ID2");
      // when
      // then
        trelloProvider.getNotesFromService();
        List<Note> noteList = trelloProvider.getNotes();
        assertEquals(1, noteList.size());
        Note firstNote = noteList.get(0);
        assertEquals("NameCard1", firstNote.getTitle());
        assertEquals("TextCard1", firstNote.getText());
    }


}
