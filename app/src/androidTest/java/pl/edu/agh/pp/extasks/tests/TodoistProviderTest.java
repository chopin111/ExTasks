package pl.edu.agh.pp.extasks.tests;

import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.cglib.core.CollectionUtils;
import org.mockito.cglib.core.Predicate;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nl.rgonline.lib.todoist.Item;
import nl.rgonline.lib.todoist.Project;
import nl.rgonline.lib.todoist.TodoistApi;
import nl.rgonline.lib.todoist.TodoistData;
import nl.rgonline.lib.todoist.TodoistException;
import pl.edu.agh.pp.extasks.framework.TodoistProvider;
import pl.edu.agh.pp.extasks.framework.exception.InitializationException;
import pl.edu.agh.pp.extasks.framework.exception.SynchronizationException;
import pl.edu.agh.pp.extasks.framework.model.Note;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.calls;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Class testing Todoist.class with Todoist from TodoistApi mocked.
 *
 * @author Jakub Lasisz
 * @author Maciej Sipko
 */
public class TodoistProviderTest extends InstrumentationTestCase {

    private Map<String, List<Item>> cardsListByBoardId = new HashMap<>();
    @Mock
    private TodoistApi todoistManagerMock;
    private TodoistProvider todoistProvider;
    private String key;
    private String token;
    @Mock
    private TodoistData data;
    @Mock
    private Project project;
    @Mock
    private Project closedProject;
    private ArrayList<Project> boardList = new ArrayList<>();
    private Item testItem;
    private ArrayList<Item> items = new ArrayList<>();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
//        getInstrumentation().getTargetContext().getCacheDir();
        System.setProperty("dexmaker.dexcache", getInstrumentation().getTargetContext().getCacheDir().getPath());
        MockitoAnnotations.initMocks(this);
        todoistProvider = new TodoistProvider("", "");
        Field field = todoistProvider.getClass().getDeclaredField("todoistManager");
        field.setAccessible(true);
        field.set(todoistProvider, todoistManagerMock);

        Constructor projectConstructor = Project.class.getDeclaredConstructors()[0];
        projectConstructor.setAccessible(true);
        Project p = (Project) projectConstructor.newInstance(data, "project1", 0, 0, 0);
        Field pData = p.getClass().getDeclaredField("data");
        pData.setAccessible(true);
        pData.set(p, data);

        boardList.add(project);
        Constructor itemConstructor = Item.class.getDeclaredConstructors()[0];
        itemConstructor.setAccessible(true);
        testItem = (Item) itemConstructor.newInstance(data, "note1", p, 0, 0, "", new Date(), 0, 0, 0);

        items.add(testItem);

        when(todoistManagerMock.get()).thenReturn(data);
        when(data.getProjects()).thenReturn(boardList);
        when(project.getName()).thenReturn("project1");
        when(project.getItems()).thenReturn(items);
    }

    @SmallTest
    public void testInitialize() {
        //given
        //when
        try {
            when(todoistManagerMock.get()).thenReturn(data);
        } catch (TodoistException e) {
            fail();
        }
        try {
            todoistProvider.initialize();
        } catch (InitializationException e) {
            fail();
        }
        //then
    }

    @SmallTest
    public void testGetNotesFromService() {
        //given
        testInitialize();
        //when
        //then
        todoistProvider.getNotesFromService();
        List<Note> noteList = todoistProvider.getNotes();
        assertEquals(1, noteList.size());
        Collection filtered = CollectionUtils.filter(noteList, new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                Note note = (Note) o;
                if (note.getTitle().equals("note1") && note.getId().equals("note1")) {
                    return true;
                }
                return false;
            }
        });
        assertEquals(1, filtered.size());
    }

    @SmallTest
    public void testGetNotesFromServiceIgnoresDeletedProjects() {
        //given
        testInitialize();
        boardList.add(closedProject);
        //when
        when(closedProject.isDeleted()).thenReturn(true);
        //then
        todoistProvider.getNotesFromService();
        List<Note> noteList = todoistProvider.getNotes();
        assertEquals(1, noteList.size());
        Collection filtered = CollectionUtils.filter(noteList, new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                Note note = (Note) o;
                if (note.getTitle().equals("note1") && note.getId().equals("note1")) {
                    return true;
                }
                return false;
            }
        });
        assertEquals(1, filtered.size());
    }

    @SmallTest
    public void testGetNotesFromServiceIgnoresArchivedProjects() {
        //given
        testInitialize();
        boardList.add(closedProject);
        //when
        when(closedProject.isIs_archived()).thenReturn(true);
        //then
        todoistProvider.getNotesFromService();
        List<Note> noteList = todoistProvider.getNotes();
        assertEquals(1, noteList.size());
        Collection filtered = CollectionUtils.filter(noteList, new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                Note note = (Note) o;
                if (note.getTitle().equals("note1") && note.getId().equals("note1")) {
                    return true;
                }
                return false;
            }
        });
        assertEquals(1, filtered.size());
    }

    @SmallTest
    public void testGetNotesFromServiceIgnoresCheckedItem() {
        //given
        testInitialize();
        items.get(0).setChecked(true);
        //when
        //then
        todoistProvider.getNotesFromService();
        List<Note> noteList = todoistProvider.getNotes();
        assertEquals(0, noteList.size());
    }

    @SmallTest
    public void testGetNotesFromServiceIgnoresArchivedtem() {
        //given
        testInitialize();
        items.get(0).setArchived(true);
        //when
        //then
        todoistProvider.getNotesFromService();
        List<Note> noteList = todoistProvider.getNotes();
        assertEquals(0, noteList.size());
    }

    @SmallTest
    public void testGetNotesFromServiceIgnoresDeletedtem() {
        //given
        testInitialize();
        items.get(0).setDeleted(true);
        //when
        //then
        todoistProvider.getNotesFromService();
        List<Note> noteList = todoistProvider.getNotes();
        assertEquals(0, noteList.size());
    }

    @SmallTest
    public void testAddNote() {
        //given
        testInitialize();
        Note n = new Note("testTitle", "testText", "", "");
        Map<String, Object> map = new HashMap<>();
        map.put("desc", "testText");
        //when
        when(data.getProjectById(1)).thenReturn(project);
        //then
        try {
            todoistProvider.addNote(n.getTitle(), n.getText(), "1");
            verify(todoistManagerMock, Mockito.times(2)).syncAndGetUpdated();
        } catch (SynchronizationException e) {
            fail();
        } catch (TodoistException e) {
            fail();
        }
    }

    @SmallTest
    public void testRemoveNote() {
        //given
        testInitialize();
        //when
        when(data.getItem("1")).thenReturn(testItem);
        //then
        try {
            todoistProvider.removeNote("1");
            verify(todoistManagerMock, Mockito.times(2)).syncAndGetUpdated();
        } catch (SynchronizationException e) {
            fail();
        } catch (TodoistException e) {
            fail();
        }
    }

    @SmallTest
    public void testEditNote() {
        //given
        testInitialize();
        //when
        when(data.getItem("1")).thenReturn(testItem);
        //then
        try {
            todoistProvider.editNote("1", "1", "1");
            verify(todoistManagerMock, Mockito.times(2)).syncAndGetUpdated();
        } catch (SynchronizationException e) {
            fail();
        } catch (TodoistException e) {
            fail();
        }
    }

    @SmallTest
    public void testEditNoteReturnsIfNoNoteWithIdFound() {
        //given
        testInitialize();
        //when
        when(data.getItem("1")).thenReturn(null);
        //then
        try {
            todoistProvider.editNote("1", "1", "1");
            verify(todoistManagerMock, Mockito.times(1)).syncAndGetUpdated();
        } catch (SynchronizationException e) {
            fail();
        } catch (TodoistException e) {
            fail();
        }
    }


}
