package com.pkmprojects.shoppiq.service;

import com.pkmprojects.shoppiq.dto.address.AddressResponse;
import com.pkmprojects.shoppiq.dto.address.CreateAddressRequest;
import com.pkmprojects.shoppiq.dto.address.UpdateAddressRequest;
import com.pkmprojects.shoppiq.entity.Address;
import com.pkmprojects.shoppiq.entity.User;
import com.pkmprojects.shoppiq.exception.AddressAccessDeniedException;
import com.pkmprojects.shoppiq.exception.AddressNotFoundException;
import com.pkmprojects.shoppiq.repository.AddressRepository;
import com.pkmprojects.shoppiq.service.impl.AddressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AddressServiceImpl}.
 *
 * <p>All dependencies are mocked. No Spring context or database is involved.</p>
 *
 * <h2>Coverage</h2>
 * <ul>
 *     <li>create()     — without default, with default, field mapping, ArgumentCaptor</li>
 *     <li>getAll()     — populated list, empty list</li>
 *     <li>getById()    — found, not found, wrong owner</li>
 *     <li>update()     — field update, new default (clears old), keep default (no clear),
 *                        unset default, not found, wrong owner</li>
 *     <li>delete()     — success, not found, wrong owner</li>
 *     <li>setDefault() — clears all then sets new, already default, not found, wrong owner</li>
 * </ul>
 *
 * @author PrabhatKrMishra
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AddressServiceImpl Tests")
class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    /**
     * Reflectively sets the private {@code id} field declared in
     * {@code BaseEntity} on any {@code AuditableEntity} subclass.
     * Address → AuditableEntity → BaseEntity (id lives 2 levels up).
     */
    private static void setId(Object entity, Long id) throws Exception {
        Field field = entity.getClass().getSuperclass().getSuperclass()
                .getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }

    private User buildUser(long id) throws Exception {
        User user = User.builder()
                .name("Alice")
                .username("alice")
                .email("alice@example.com")
                .password("hashed")
                .enabled(true)
                .build();
        setId(user, id);
        return user;
    }

    private Address buildAddress(long id, User owner, boolean isDefault) throws Exception {
        Address addr = Address.builder()
                .user(owner)
                .label("Home")
                .fullName("Alice Smith")
                .phone("9876543210")
                .line1("221B Baker Street")
                .line2("Near Chandni Chowk")
                .city("Delhi")
                .state("Delhi")
                .postalCode("110001")
                .country("India")
                .isDefault(isDefault)
                .build();
        setId(addr, id);
        return addr;
    }

    private CreateAddressRequest createRequest(boolean isDefault) {
        return new CreateAddressRequest(
                "Home", "Alice Smith", "9876543210",
                "221B Baker Street", "Near Chandni Chowk",
                "Delhi", "Delhi", "110001", "India", isDefault
        );
    }

    private UpdateAddressRequest updateRequest(boolean isDefault) {
        return new UpdateAddressRequest(
                "Office", "Alice Smith", "9876543210",
                "42 Connaught Place", null,
                "Delhi", "Delhi", "110001", "India", isDefault
        );
    }

    // ---------------------------------------------------------------
    // Shared fixtures
    // ---------------------------------------------------------------

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() throws Exception {
        alice = buildUser(1L);
        bob = buildUser(2L);
    }

    // ---------------------------------------------------------------
    // create()
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("Saves address and returns response — no default flag")
        void create_withoutDefault_savesAddress() throws Exception {
            Address saved = buildAddress(10L, alice, false);
            when(addressRepository.save(any())).thenReturn(saved);

            AddressResponse resp = addressService.create(alice, createRequest(false));

            verify(addressRepository, never()).clearDefaultForUser(any());
            verify(addressRepository).save(any(Address.class));
            assertThat(resp.id()).isEqualTo(10L);
            assertThat(resp.isDefault()).isFalse();
        }

        @Test
        @DisplayName("Clears existing default before saving — when request.isDefault = true")
        void create_withDefault_clearsExistingDefaultFirst() throws Exception {
            Address saved = buildAddress(11L, alice, true);
            when(addressRepository.save(any())).thenReturn(saved);

            addressService.create(alice, createRequest(true));

            // clearDefaultForUser must precede save
            var order = inOrder(addressRepository);
            order.verify(addressRepository).clearDefaultForUser(alice);
            order.verify(addressRepository).save(any(Address.class));
        }

        @Test
        @DisplayName("Returned response reflects the saved address default flag")
        void create_withDefault_responseReflectsDefault() throws Exception {
            Address saved = buildAddress(12L, alice, true);
            when(addressRepository.save(any())).thenReturn(saved);

            AddressResponse resp = addressService.create(alice, createRequest(true));

            assertThat(resp.isDefault()).isTrue();
        }

        @Test
        @DisplayName("Maps all request fields onto the persisted Address entity")
        void create_mapsAllFieldsToEntity() throws Exception {
            ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
            Address saved = buildAddress(13L, alice, false);
            when(addressRepository.save(captor.capture())).thenReturn(saved);

            addressService.create(alice, createRequest(false));

            Address captured = captor.getValue();
            assertThat(captured.getUser()).isSameAs(alice);
            assertThat(captured.getLabel()).isEqualTo("Home");
            assertThat(captured.getFullName()).isEqualTo("Alice Smith");
            assertThat(captured.getPhone()).isEqualTo("9876543210");
            assertThat(captured.getLine1()).isEqualTo("221B Baker Street");
            assertThat(captured.getLine2()).isEqualTo("Near Chandni Chowk");
            assertThat(captured.getCity()).isEqualTo("Delhi");
            assertThat(captured.getState()).isEqualTo("Delhi");
            assertThat(captured.getPostalCode()).isEqualTo("110001");
            assertThat(captured.getCountry()).isEqualTo("India");
        }

        @Test
        @DisplayName("Null line2 is passed through without throwing")
        void create_nullLine2_doesNotThrow() throws Exception {
            CreateAddressRequest req = new CreateAddressRequest(
                    "Home", "Alice Smith", "9876543210",
                    "221B Baker Street", null,
                    "Delhi", "Delhi", "110001", "India", false
            );
            Address saved = buildAddress(14L, alice, false);
            when(addressRepository.save(any())).thenReturn(saved);

            assertThat(addressService.create(alice, req)).isNotNull();
        }
    }

    // ---------------------------------------------------------------
    // getAll()
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("getAll()")
    class GetAllTests {

        @Test
        @DisplayName("Returns mapped response list for the user")
        void getAll_returnsMappedList() throws Exception {
            Address a1 = buildAddress(1L, alice, true);
            Address a2 = buildAddress(2L, alice, false);
            when(addressRepository.findAllByUser(alice)).thenReturn(List.of(a1, a2));

            List<AddressResponse> result = addressService.getAll(alice);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).id()).isEqualTo(1L);
            assertThat(result.get(1).id()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Returns empty list when user has no addresses")
        void getAll_noAddresses_returnsEmpty() {
            when(addressRepository.findAllByUser(alice)).thenReturn(List.of());

            assertThat(addressService.getAll(alice)).isEmpty();
        }

        @Test
        @DisplayName("Passes the correct user to the repository")
        void getAll_queriesRepositoryWithCorrectUser() {
            when(addressRepository.findAllByUser(alice)).thenReturn(List.of());

            addressService.getAll(alice);

            verify(addressRepository).findAllByUser(alice);
            verify(addressRepository, never()).findAllByUser(bob);
        }
    }

    // ---------------------------------------------------------------
    // getById()
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("Returns response when address exists and belongs to user")
        void getById_ownedAddress_returnsResponse() throws Exception {
            Address addr = buildAddress(5L, alice, false);
            when(addressRepository.findById(5L)).thenReturn(Optional.of(addr));

            AddressResponse resp = addressService.getById(alice, 5L);

            assertThat(resp.id()).isEqualTo(5L);
        }

        @Test
        @DisplayName("Throws AddressNotFoundException when address does not exist")
        void getById_notFound_throwsNotFoundException() {
            when(addressRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> addressService.getById(alice, 99L))
                    .isInstanceOf(AddressNotFoundException.class);
        }

        @Test
        @DisplayName("Throws AddressAccessDeniedException when address belongs to another user")
        void getById_wrongOwner_throwsAccessDeniedException() throws Exception {
            Address addr = buildAddress(5L, bob, false);
            when(addressRepository.findById(5L)).thenReturn(Optional.of(addr));

            assertThatThrownBy(() -> addressService.getById(alice, 5L))
                    .isInstanceOf(AddressAccessDeniedException.class);
        }

        @Test
        @DisplayName("Does not invoke save or delete during read")
        void getById_neverWritesToRepository() throws Exception {
            Address addr = buildAddress(5L, alice, false);
            when(addressRepository.findById(5L)).thenReturn(Optional.of(addr));

            addressService.getById(alice, 5L);

            verify(addressRepository, never()).save(any());
            verify(addressRepository, never()).delete(any());
        }
    }

    // ---------------------------------------------------------------
    // update()
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Test
        @DisplayName("Updates and saves all mutable fields")
        void update_updatesAllFields() throws Exception {
            Address addr = buildAddress(5L, alice, false);
            when(addressRepository.findById(5L)).thenReturn(Optional.of(addr));
            when(addressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            AddressResponse resp = addressService.update(alice, 5L, updateRequest(false));

            assertThat(resp.label()).isEqualTo("Office");
            assertThat(resp.line1()).isEqualTo("42 Connaught Place");
            assertThat(resp.line2()).isNull();
        }

        @Test
        @DisplayName("Persists the updated entity")
        void update_savesUpdatedEntity() throws Exception {
            Address addr = buildAddress(5L, alice, false);
            when(addressRepository.findById(5L)).thenReturn(Optional.of(addr));
            when(addressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            addressService.update(alice, 5L, updateRequest(false));

            verify(addressRepository).save(addr);
        }

        @Test
        @DisplayName("Clears existing default when address changes to default")
        void update_newDefaultSet_clearsOldDefault() throws Exception {
            Address addr = buildAddress(5L, alice, false); // currently NOT default
            when(addressRepository.findById(5L)).thenReturn(Optional.of(addr));
            when(addressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            addressService.update(alice, 5L, updateRequest(true));

            var order = inOrder(addressRepository);
            order.verify(addressRepository).clearDefaultForUser(alice);
            order.verify(addressRepository).save(addr);
        }

        @Test
        @DisplayName("Does NOT clear default when address is already the default and stays default")
        void update_keepDefault_doesNotClearDefault() throws Exception {
            Address addr = buildAddress(5L, alice, true); // already default
            when(addressRepository.findById(5L)).thenReturn(Optional.of(addr));
            when(addressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            addressService.update(alice, 5L, updateRequest(true));

            verify(addressRepository, never()).clearDefaultForUser(any());
        }

        @Test
        @DisplayName("Does NOT clear default when unsetting the default flag")
        void update_unsetDefault_doesNotClearDefault() throws Exception {
            Address addr = buildAddress(5L, alice, true); // currently default, being unset
            when(addressRepository.findById(5L)).thenReturn(Optional.of(addr));
            when(addressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            addressService.update(alice, 5L, updateRequest(false));

            verify(addressRepository, never()).clearDefaultForUser(any());
        }

        @Test
        @DisplayName("Throws AddressNotFoundException when address does not exist")
        void update_notFound_throwsNotFoundException() {
            when(addressRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> addressService.update(alice, 99L, updateRequest(false)))
                    .isInstanceOf(AddressNotFoundException.class);

            verify(addressRepository, never()).save(any());
        }

        @Test
        @DisplayName("Throws AddressAccessDeniedException when address belongs to another user")
        void update_wrongOwner_throwsAccessDeniedException() throws Exception {
            Address addr = buildAddress(5L, bob, false);
            when(addressRepository.findById(5L)).thenReturn(Optional.of(addr));

            assertThatThrownBy(() -> addressService.update(alice, 5L, updateRequest(false)))
                    .isInstanceOf(AddressAccessDeniedException.class);

            verify(addressRepository, never()).save(any());
        }
    }

    // ---------------------------------------------------------------
    // delete()
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("Deletes the address when it belongs to the user")
        void delete_ownedAddress_deletesSuccessfully() throws Exception {
            Address addr = buildAddress(5L, alice, false);
            when(addressRepository.findById(5L)).thenReturn(Optional.of(addr));

            addressService.delete(alice, 5L);

            verify(addressRepository).delete(addr);
        }

        @Test
        @DisplayName("Does not call save when deleting")
        void delete_doesNotSave() throws Exception {
            Address addr = buildAddress(5L, alice, false);
            when(addressRepository.findById(5L)).thenReturn(Optional.of(addr));

            addressService.delete(alice, 5L);

            verify(addressRepository, never()).save(any());
        }

        @Test
        @DisplayName("Throws AddressNotFoundException when address does not exist")
        void delete_notFound_throwsNotFoundException() {
            when(addressRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> addressService.delete(alice, 99L))
                    .isInstanceOf(AddressNotFoundException.class);

            verify(addressRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Throws AddressAccessDeniedException when address belongs to another user")
        void delete_wrongOwner_throwsAccessDeniedException() throws Exception {
            Address addr = buildAddress(5L, bob, false);
            when(addressRepository.findById(5L)).thenReturn(Optional.of(addr));

            assertThatThrownBy(() -> addressService.delete(alice, 5L))
                    .isInstanceOf(AddressAccessDeniedException.class);

            verify(addressRepository, never()).delete(any());
        }
    }

    // ---------------------------------------------------------------
    // setDefault()
    // ---------------------------------------------------------------

    @Nested
    @DisplayName("setDefault()")
    class SetDefaultTests {

        @Test
        @DisplayName("Clears all user defaults then marks the chosen address as default")
        void setDefault_clearsOldThenSetsNew() throws Exception {
            Address addr = buildAddress(5L, alice, false);
            when(addressRepository.findById(5L)).thenReturn(Optional.of(addr));
            when(addressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            AddressResponse resp = addressService.setDefault(alice, 5L);

            var order = inOrder(addressRepository);
            order.verify(addressRepository).clearDefaultForUser(alice);
            order.verify(addressRepository).save(addr);

            assertThat(resp.isDefault()).isTrue();
        }

        @Test
        @DisplayName("Also clears and re-sets when address is already the default")
        void setDefault_alreadyDefault_stillClearsAndSets() throws Exception {
            Address addr = buildAddress(5L, alice, true); // already default
            when(addressRepository.findById(5L)).thenReturn(Optional.of(addr));
            when(addressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            AddressResponse resp = addressService.setDefault(alice, 5L);

            verify(addressRepository).clearDefaultForUser(alice);
            assertThat(resp.isDefault()).isTrue();
        }

        @Test
        @DisplayName("Throws AddressNotFoundException when address does not exist")
        void setDefault_notFound_throwsNotFoundException() {
            when(addressRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> addressService.setDefault(alice, 99L))
                    .isInstanceOf(AddressNotFoundException.class);

            verify(addressRepository, never()).clearDefaultForUser(any());
            verify(addressRepository, never()).save(any());
        }

        @Test
        @DisplayName("Throws AddressAccessDeniedException when address belongs to another user")
        void setDefault_wrongOwner_throwsAccessDeniedException() throws Exception {
            Address addr = buildAddress(5L, bob, false);
            when(addressRepository.findById(5L)).thenReturn(Optional.of(addr));

            assertThatThrownBy(() -> addressService.setDefault(alice, 5L))
                    .isInstanceOf(AddressAccessDeniedException.class);

            verify(addressRepository, never()).clearDefaultForUser(any());
            verify(addressRepository, never()).save(any());
        }
    }
}