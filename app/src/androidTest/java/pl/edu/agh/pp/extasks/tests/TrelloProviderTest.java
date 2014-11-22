package pl.edu.agh.pp.extasks.tests;

import android.test.suitebuilder.annotation.SmallTest;

import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.cglib.core.CollectionUtils;
import org.mockito.cglib.core.Predicate;
import org.trello4j.Trello;
import org.trello4j.model.Board;
import org.trello4j.model.Card;
import org.trello4j.model.Member;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import pl.edu.agh.pp.extasks.framework.TrelloProvider;
import pl.edu.agh.pp.extasks.framework.exception.InitializationException;
import pl.edu.agh.pp.extasks.framework.model.Note;

import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Class testing TrelloProvider.class with Trello from Trello4J mocked.
 *
 * @author Jakub Lasisz
 * @author Maciej Sipko
 */
public class TrelloProviderTest extends TestCase {
    private Map<String, List<Card>> cardsListByBoardId = new HashMap<>();
    @Mock
    private Trello trelloManagerMock;
    private TrelloProvider trelloProvider;
    private String key;
    private String token;
    private Member member;
    private List<Board> boardList = new LinkedList<Board>();
    private List<org.trello4j.model.List> listofLists = new LinkedList<>();
    private Card testCard;

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
        addList("ID1", "ID1");
        when(trelloManagerMock.getMemberByToken(token)).thenReturn(member);
        when(trelloManagerMock.getBoardsByMember(member.getId())).thenReturn(boardList);
        when(trelloManagerMock.getListByBoard("ID1")).thenReturn(listofLists);
        when(trelloManagerMock.getCardsByBoard("ID1")).thenReturn(cardsListByBoardId.get("ID1"));

    }

    private void injectTrelloMock() {
        try {
            Field field = trelloProvider.getClass().getDeclaredField("trelloManager");
            field.setAccessible(true);
            field.set(trelloProvider, trelloManagerMock);
        } catch (IllegalAccessException | NoSuchFieldException e) {
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
        testCard = new Card();
        testCard.setId(id);
        testCard.setName(name);
        testCard.setDesc(text);
        if (!cardsListByBoardId.containsKey(boardId)) {
            cardsListByBoardId.put(boardId, new LinkedList<Card>());
        }
        cardsListByBoardId.get(boardId).add(testCard);
    }

    private void addList(String idList, String idBoard) {
        org.trello4j.model.List list = new org.trello4j.model.List();
        list.setId(idList);
        list.setClosed(false);
        list.setIdBoard(idBoard);
        list.setName(idList);
        listofLists.add(list);
    }

    @SmallTest
    public void testInitialize() {
        //given
        addBoard("ID2", "Name2");
        addCard("IDCard2", "NameCard2", "TextCard2", "ID2");
        //when
        when(trelloManagerMock.getCardsByBoard("ID2")).thenReturn(cardsListByBoardId.get("ID2"));
        //then
        try {
            trelloProvider.initialize();
        } catch (InitializationException e) {
            fail();
        }
    }

    /**
     * Tests consistency and correctness of recieved Notes via TrelloProvider.
     */
    @SmallTest
    public void testGetNotesFromService() {
        //given
        testInitialize();
        //when
        //then
        trelloProvider.getNotesFromService();
        List<Note> noteList = trelloProvider.getNotes();
        assertEquals(2, noteList.size());
        Collection filtered = CollectionUtils.filter(noteList, new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                Note note = (Note) o;
                if (note.getTitle().equals("NameCard1") && note.getText().equals("TextCard1")) {
                    return true;
                }
                if (note.getTitle().equals("NameCard2") && note.getText().equals("TextCard2")) {
                    return true;
                }
                return false;
            }
        });
        assertEquals(2, filtered.size());
    }

    /**
     * Ensures that closed boards are omitted during recieving Notes via TrelloProvider.
     */
    @SmallTest
    public void testGetNotesFromServiceWithClosedBoard() {
        // given
        Board board = new Board();
        board.setId("ID3");
        board.setName("Name3");
        board.setClosed(true);
        boardList.add(board);
        addCard("IDCard2", "NameCard2", "TextCard2", "ID3");
        testInitialize();
        // when
        // then
        trelloProvider.getNotesFromService();
        List<Note> noteList = trelloProvider.getNotes();
        assertEquals(2, noteList.size());
        Collection filtered = CollectionUtils.filter(noteList, new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                Note note = (Note) o;
                if (note.getTitle().equals("NameCard1") && note.getText().equals("TextCard1")) {
                    return true;
                }
                if (note.getTitle().equals("NameCard2") && note.getText().equals("TextCard2")) {
                    return true;
                }
                return false;
            }
        });
        assertEquals(2, filtered.size());
    }

    @SmallTest
    public void testAddNote() {
        //given
        Note n = new Note("testTitle", "testText", "", "");
        trelloProvider.addNote(n.getTitle(), n.getText(), listofLists.get(0).getId());
        Map<String, Object> map = new HashMap<>();
        map.put("desc", "testText");
        //when

        //then
        verify(trelloManagerMock, only()).createCard(listofLists.get(0).getId(), n.getTitle(), map);
    }

    @SmallTest
    public void testRemoveNote() {
        //given
        trelloProvider.removeNote(testCard.getId());
        //when

        //then
        verify(trelloManagerMock, only()).deleteCard(testCard.getId());
    }

    @SmallTest
    public void testEditNote() {
        //given
        trelloProvider.editNote(testCard.getId(), "newTitle", "newText");
        //when

        //then
        verify(trelloManagerMock, only()).updateCard(testCard.getId(), "newTitle", "newText");
    }

}
